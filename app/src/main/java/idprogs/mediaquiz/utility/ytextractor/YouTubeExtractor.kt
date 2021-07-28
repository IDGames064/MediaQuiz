package idprogs.mediaquiz.utility.ytextractor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import com.evgenii.jsevaluator.JsEvaluator
import com.evgenii.jsevaluator.interfaces.JsCallback
import idprogs.mediaquiz.di.DispatcherProvider
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Matcher
import java.util.regex.Pattern


class YouTubeExtractor(context: Context, private val http: HttpClient, private val dispatchers: DispatcherProvider) {

    private val CACHING = true
    var LOGGING = true
    private val LOG_TAG = "YouTubeExtractor"
    private val CACHE_FILE_NAME = "decipher_js_funct"

    private val refContext: WeakReference<Context> = WeakReference(context)

    @Volatile
    private var decipheredSignature: String? = null

    private var decipherJsFileName: String? = null
    private var decipherFunctions: String? = null
    private var decipherFunctionName: String? = null
    private val cacheDirPath: String? = context.cacheDir.absolutePath

    private val lock: Lock = ReentrantLock()
    private val jsExecuting = lock.newCondition()

    private val patYouTubePageLink = Pattern.compile("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)")
    private val patYouTubeShortLink = Pattern.compile("(http|https)://(www\\.|)youtu.be/(.+?)( |\\z|&)")
    private val patVariableFunction = Pattern.compile("([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\\(")
    private val patFunction = Pattern.compile("([{; =])([a-zA-Z\$_][a-zA-Z0-9$]{0,2})\\(")
    private val patDecryptionJsFile = Pattern.compile("\\\\/s\\\\/player\\\\/([^\"]+?)\\.js")
    private val patDecryptionJsFileWithoutSlash = Pattern.compile("/s/player/([^\"]+?).js")
    private val patSignatureDecFunction = Pattern.compile("(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)")

    private val patPlayerResponse = Pattern.compile("var ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\})\\s*;")
    private val patSigEncUrl = Pattern.compile("url=(.+?)(\\u0026|$)")
    private val patSignature = Pattern.compile("s=(.+?)(\\u0026|$)")

    private val FORMAT_MAP = mapOf(
            Pair(18, Format(18, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 96, false)),
            Pair(22, Format(22, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 192, false))
    )

    suspend fun extract(youtubeLink: String): Map<Int, YtFile>? = withContext(dispatchers.io) {
        var videoID: String? = null
        var mat = patYouTubePageLink.matcher(youtubeLink)
        if (mat.find()) {
            videoID = mat.group(3)
        } else {
            mat = patYouTubeShortLink.matcher(youtubeLink)
            if (mat.find()) {
                videoID = mat.group(3)
            } else if (youtubeLink.matches(Regex("\\p{Graph}+?"))) {
                videoID = youtubeLink
            }
        }
        if (videoID != null) {
            try {
                return@withContext getStreamUrls(videoID)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            Log.e(LOG_TAG, "Wrong YouTube link format")
        }
        null
    }

    @Throws(IOException::class, InterruptedException::class, JSONException::class)
    private fun getStreamUrls(videoId: String): Map<Int, YtFile>? {
        val encSignatures = SparseArray<String?>()
        val ytFiles = HashMap<Int, YtFile>()

        val ytInfoUrl = "https://youtube.com/watch?v=$videoId"

        val sbPageHtml = StringBuilder()
        http.get(ytInfoUrl) {
            sbPageHtml.append(it)
        }
        val pageHtml = sbPageHtml.toString()

        var mat: Matcher = patPlayerResponse.matcher(pageHtml)
        if (mat.find()) {
            val ytPlayerResponse = JSONObject(mat.group(1))
            val streamingData = ytPlayerResponse.getJSONObject("streamingData")
            val formats = streamingData.getJSONArray("formats")
            for (i in 0 until formats.length()) {
                val format = formats.getJSONObject(i)
                val itag = format.getInt("itag")
                if (FORMAT_MAP[itag] != null) {
                    if (format.has("url")) {
                        val url = format.getString("url").replace("\\u0026", "&")
                        ytFiles[itag] = YtFile(FORMAT_MAP[itag]!!, url)
                    } else if (format.has("signatureCipher")) {
                        mat = patSigEncUrl.matcher(format.getString("signatureCipher"))
                        val matSig: Matcher =
                            patSignature.matcher(format.getString("signatureCipher"))
                        if (mat.find() && matSig.find()) {
                            val url = URLDecoder.decode(mat.group(1), "UTF-8")
                            val signature = URLDecoder.decode(matSig.group(1), "UTF-8")
                            ytFiles[itag] = YtFile(FORMAT_MAP[itag]!!, url)
                            encSignatures.append(itag, signature)
                        }
                    }
                }
            }
            val adaptiveFormats = streamingData.getJSONArray("adaptiveFormats")
            for (i in 0 until adaptiveFormats.length()) {
                val adaptiveFormat = adaptiveFormats.getJSONObject(i)
                val itag = adaptiveFormat.getInt("itag")
                if (FORMAT_MAP[itag] != null) {
                    if (adaptiveFormat.has("url")) {
                        val url = adaptiveFormat.getString("url").replace("\\u0026", "&")
                        ytFiles[itag] = YtFile(FORMAT_MAP[itag]!!, url)
                    } else if (adaptiveFormat.has("signatureCipher")) {
                        mat = patSigEncUrl.matcher(adaptiveFormat.getString("signatureCipher"))
                        val matSig: Matcher =
                            patSignature.matcher(adaptiveFormat.getString("signatureCipher"))
                        if (mat.find() && matSig.find()) {
                            val url = URLDecoder.decode(mat.group(1), "UTF-8")
                            val signature = URLDecoder.decode(matSig.group(1), "UTF-8")
                            ytFiles[itag] = YtFile(FORMAT_MAP[itag]!!, url)
                            encSignatures.append(itag, signature)
                        }
                    }
                }
            }
/*
            val videoDetails = ytPlayerResponse.getJSONObject("videoDetails")
            this.videoMeta = VideoMeta(
                videoDetails.getString("videoId"),
                videoDetails.getString("title"),
                videoDetails.getString("author"),
                videoDetails.getString("channelId"),
                videoDetails.getString("lengthSeconds").toLong(),
                videoDetails.getString("viewCount").toLong(),
                videoDetails.getBoolean("isLiveContent"),
                videoDetails.getString("shortDescription")
            )
*/
        } else {
            Log.d(LOG_TAG, "ytPlayerResponse was not found")
        }
        if (encSignatures.size() > 0) {
            val curJsFileName: String
            if (CACHING && (decipherJsFileName == null || decipherFunctions == null || decipherFunctionName == null)) {
                readDecipherFuncFromCache()
            }
            mat = patDecryptionJsFile.matcher(pageHtml)
            if (!mat.find()) mat = patDecryptionJsFileWithoutSlash.matcher(pageHtml)
            if (mat.find()) {
                curJsFileName = mat.group(0).replace("\\/", "/")
                if (decipherJsFileName == null || decipherJsFileName != curJsFileName) {
                    decipherFunctions = null
                    decipherFunctionName = null
                }
                decipherJsFileName = curJsFileName
            }
            if (LOGGING) Log.d(
                LOG_TAG,
                "Decipher signatures: " + encSignatures.size() + ", videos: " + ytFiles.size
            )
            decipheredSignature = null
            if (decipherSignature(encSignatures)) {
                lock.lock()
                try {
                    jsExecuting.await(7, TimeUnit.SECONDS)
                } finally {
                    lock.unlock()
                }
            }
            val signature: String? = decipheredSignature
            if (signature == null) {
                return null
            } else {
                val sigs = signature.split("\n").toTypedArray()
                var i = 0
                while (i < encSignatures.size() && i < sigs.size) {
                    val key = encSignatures.keyAt(i)
                    var url = ytFiles[key]?.url
                    url += "&sig=" + sigs[i]
                    val newFile = YtFile(FORMAT_MAP[key]!!, url!!)
                    ytFiles[key] = newFile
                    i++
                }
            }
        }
        if (ytFiles.isEmpty()) {
            if (LOGGING) Log.d(LOG_TAG, pageHtml)
            return null
        }
        return ytFiles
    }


    @Throws(IOException::class)
    private fun decipherSignature(encSignatures: SparseArray<String?>): Boolean {
        // Assume the functions don't change that much
        if (decipherFunctionName == null || decipherFunctions == null) {
            val decipherFuncUrl = "https://youtube.com$decipherJsFileName"
            val javascriptFile: String
            val sb = StringBuilder()
            http.get(decipherFuncUrl) {
                sb.append(it)
                sb.append(" ")
            }
            javascriptFile = sb.toString()

            if (LOGGING) Log.d(LOG_TAG, "Decipher FunctURL: $decipherFuncUrl")
            var mat = patSignatureDecFunction.matcher(javascriptFile)
            if (mat.find()) {
                decipherFunctionName = mat.group(1)
                if (LOGGING) Log.d(LOG_TAG, "Decipher Functname: $decipherFunctionName")
                val patMainVariable = Pattern.compile("(var |\\s|,|;)" + decipherFunctionName!!.replace("$", "\\$") + "(=function\\((.{1,3})\\)\\{)")
                var mainDecipherFunct: String
                mat = patMainVariable.matcher(javascriptFile)
                if (mat.find()) {
                    mainDecipherFunct = "var " + decipherFunctionName + mat.group(2)
                } else {
                    val patMainFunction = Pattern.compile("function " + decipherFunctionName!!.replace("$","\\$") + "(\\((.{1,3})\\)\\{)")
                    mat = patMainFunction.matcher(javascriptFile)
                    if (!mat.find()) return false
                    mainDecipherFunct = "function " + decipherFunctionName + mat.group(2)
                }
                var startIndex = mat.end()
                var braces = 1
                var i = startIndex
                while (i < javascriptFile.length) {
                    if (braces == 0 && startIndex + 5 < i) {
                        mainDecipherFunct += javascriptFile.substring(startIndex, i) + ";"
                        break
                    }
                    if (javascriptFile[i] == '{') braces++ else if (javascriptFile[i] == '}') braces--
                    i++
                }
                decipherFunctions = mainDecipherFunct
                // Search the main function for extra functions and variables
                // needed for deciphering
                // Search for variables
                mat = patVariableFunction.matcher(mainDecipherFunct)
                while (mat.find()) {
                    val variableDef = "var " + mat.group(2) + "={"
                    if (decipherFunctions!!.contains(variableDef)) continue
                    startIndex = javascriptFile.indexOf(variableDef) + variableDef.length
                    braces = 1
                    i = startIndex
                    while (i < javascriptFile.length) {
                        if (braces == 0) {
                            decipherFunctions += variableDef + javascriptFile.substring(startIndex, i) + ";"
                            break
                        }
                        if (javascriptFile[i] == '{') braces++ else if (javascriptFile[i] == '}') braces--
                        i++
                    }
                }
                // Search for functions
                mat = patFunction.matcher(mainDecipherFunct)
                while (mat.find()) {
                    val functionDef = "function " + mat.group(2) + "("
                    if (decipherFunctions!!.contains(functionDef)) continue
                    startIndex = javascriptFile.indexOf(functionDef) + functionDef.length
                    braces = 0
                    i = startIndex
                    while (i < javascriptFile.length) {
                        if (braces == 0 && startIndex + 5 < i) {
                            decipherFunctions += functionDef + javascriptFile.substring(startIndex, i) + ";"
                            break
                        }
                        if (javascriptFile[i] == '{') braces++ else if (javascriptFile[i] == '}') braces--
                        i++
                    }
                }
                if (LOGGING) Log.d(LOG_TAG, "Decipher Function: $decipherFunctions")
                decipherViaWebView(encSignatures)
                if (CACHING) writeDecipherFuncToCache()

            } else {
                return false
            }
        } else {
            decipherViaWebView(encSignatures)
        }
        return true
    }

    private fun readDecipherFuncFromCache() {
        val cacheFile = File("$cacheDirPath/$CACHE_FILE_NAME")
        // The cached functions are valid for 2 weeks
        if (cacheFile.exists() && System.currentTimeMillis() - cacheFile.lastModified() < 1209600000) {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(FileInputStream(cacheFile), "UTF-8"))
                decipherJsFileName = reader.readLine()
                decipherFunctionName = reader.readLine()
                decipherFunctions = reader.readLine()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun writeDecipherFuncToCache() {
        val cacheFile = File("$cacheDirPath/$CACHE_FILE_NAME")
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(OutputStreamWriter(FileOutputStream(cacheFile), "UTF-8"))
            writer.run {
                write("""$decipherJsFileName""".trimIndent())
                write("""$decipherFunctionName""".trimIndent())
                write(decipherFunctions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun decipherViaWebView(encSignatures: SparseArray<String?>) {
        val context = refContext.get() ?: return
        val stb = StringBuilder("$decipherFunctions function decipher(")
        stb.append("){return ")
        for (i in 0 until encSignatures.size()) {
            val key = encSignatures.keyAt(i)
            if (i < encSignatures.size() - 1) stb.append(decipherFunctionName).append("('").append(encSignatures[key]).append("')+\"\\n\"+")
            else stb.append(decipherFunctionName).append("('").append(encSignatures[key]).append("')")
        }
        stb.append("};decipher();")
        Handler(Looper.getMainLooper()).post {
            JsEvaluator(context).evaluate(stb.toString(), object : JsCallback {
                override fun onResult(result: String) {
                    lock.lock()
                    try {
                        decipheredSignature = result
                        jsExecuting.signal()
                    } finally {
                        lock.unlock()
                    }
                }

                override fun onError(errorMessage: String) {
                    lock.lock()
                    try {
                        if (LOGGING) Log.e(LOG_TAG, errorMessage)
                        jsExecuting.signal()
                    } finally {
                        lock.unlock()
                    }
                }
            })
        }
    }
}
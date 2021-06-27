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
    private val patStatusOk = Pattern.compile("status=ok(&|,|\\z)")
    private val patItag = Pattern.compile("itag=([0-9]+?)(&|\\z)")
    private val patEncSig = Pattern.compile("s=(.{10,}?)(\\\\\\\\u0026|\\z)")
    private val patUrl = Pattern.compile("\"url\"\\s*:\\s*\"(.+?)\"")
    private val patCipher = Pattern.compile("\"signatureCipher\"\\s*:\\s*\"(.+?)\"")
    private val patCipherUrl = Pattern.compile("url=(.+?)(\\\\\\\\u0026|\\z)")
    private val patVariableFunction = Pattern.compile("([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\\(")
    private val patFunction = Pattern.compile("([{; =])([a-zA-Z\$_][a-zA-Z0-9$]{0,2})\\(")
    private val patDecryptionJsFile = Pattern.compile("\\\\/s\\\\/player\\\\/([^\"]+?)\\.js")
    private val patDecryptionJsFileWithoutSlash = Pattern.compile("/s/player/([^\"]+?).js")
    private val patSignatureDecFunction = Pattern.compile("(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)")

    private val FORMAT_MAP = mapOf(
            Pair(18, Format(18, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 96, false)),
            Pair(22, Format(22, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 192, false))
    )

    private fun String.urlEncode(): String {
        return URLEncoder.encode(this, Charsets.UTF_8.name())
    }

    private fun String.urlDecode(): String {
        return URLDecoder.decode(this, Charsets.UTF_8.name())
    }

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

    @Throws(IOException::class, InterruptedException::class)
    private fun getStreamUrls(videoId: String): Map<Int, YtFile>? {
        val ytInfoUrl = "https://www.youtube.com/get_video_info?video_id=$videoId&html5=1&c=TVHTML5&cver=6.20180913&eurl=" + "https://youtube.googleapis.com/v/$videoId".urlEncode()

        var streamMap = ""
        http.get(ytInfoUrl) { streamMap = it }

        var mat: Matcher
        val curJsFileName: String
        var encSignatures: SparseArray<String?>? = null
        streamMap = streamMap.urlDecode().replace("\\u0026", "&")

        // "use_cipher_signature" disappeared, we check whether at least one ciphered signature
        // exists int the stream_map.
        var sigEnc = true
        var statusFail = false
        if (!patCipher.matcher(streamMap).find()) {
            sigEnc = false
            if (!patStatusOk.matcher(streamMap).find()) {
                statusFail = true
            }
        }

        // Some videos are using a ciphered signature we need to get the
        // deciphering js-file from the youtubepage.
        if (sigEnc || statusFail) {
            // Get the video directly from the youtubepage
            if (CACHING && (decipherJsFileName == null || decipherFunctions == null || decipherFunctionName == null)) {
                readDecipherFuncFromCache()
            }
            if (LOGGING) Log.d(LOG_TAG, "Get from youtube page")

            val sbStreamMap = java.lang.StringBuilder()
            http.get("https://youtube.com/watch?v=$videoId") {
                sbStreamMap.append(it.replace("\\\"", "\""))
            }
            streamMap = sbStreamMap.toString()

            encSignatures = SparseArray()
            mat = patDecryptionJsFile.matcher(streamMap)
            if (!mat.find()) mat = patDecryptionJsFileWithoutSlash.matcher(streamMap)
            if (mat.find()) {
                curJsFileName = mat.group(0).replace("\\/", "/")
                if (decipherJsFileName == null || decipherJsFileName != curJsFileName) {
                    decipherFunctions = null
                    decipherFunctionName = null
                }
                decipherJsFileName = curJsFileName
            }
        }

        val ytFiles = HashMap<Int, YtFile>()
        mat = if (sigEnc) {
            patCipher.matcher(streamMap)
        } else {
            patUrl.matcher(streamMap)
        }
        while (mat.find()) {
            var sig: String? = null
            var url: String
            if (sigEnc) {
                val cipher = mat.group(1)
                var mat2 = patCipherUrl.matcher(cipher)
                if (mat2.find()) {
                    url = URLDecoder.decode(mat2.group(1), "UTF-8")
                    mat2 = patEncSig.matcher(cipher)
                    if (mat2.find()) {
                        sig = URLDecoder.decode(mat2.group(1), "UTF-8")
                        // fix issue #165
                        sig = sig.replace("\\u0026", "&")
                        sig = sig.split("&".toRegex()).toTypedArray()[0]
                    } else {
                        continue
                    }
                } else {
                    continue
                }
            } else {
                url = mat.group(1)
            }
            val mat2 = patItag.matcher(url)
            if (!mat2.find()) continue
            val itag = mat2.group(1).toInt()
            if (FORMAT_MAP[itag] == null) {
                //if (LOGGING) Log.d(LOG_TAG, "Itag not in list:$itag");
                continue
            }

            // Unsupported
            if (url.contains("&source=yt_otf&")) continue
            if (LOGGING) Log.d(LOG_TAG, "Itag found:$itag")
            if (sig != null) {
                encSignatures!!.append(itag, sig)
            }
            val format = FORMAT_MAP[itag]
            val newVideo = YtFile(format!!, url)
            ytFiles[itag] = newVideo
        }
        if (encSignatures != null) {
            if (LOGGING) Log.d(LOG_TAG, "Decipher signatures: " + encSignatures.size() + ", videos: " + ytFiles.size
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
                val sigs = signature.split("\n".toRegex()).toTypedArray()
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
            if (LOGGING) Log.d(LOG_TAG, streamMap)
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
            val sb = java.lang.StringBuilder("")
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
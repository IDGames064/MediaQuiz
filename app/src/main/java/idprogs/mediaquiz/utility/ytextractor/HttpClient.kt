package idprogs.mediaquiz.utility.ytextractor

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class HttpClient @Inject constructor() {
    fun get(url: String, lineReader: (String) -> Unit) {
        val getUrl = URL(url)
        val urlConnection = getUrl.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("User-Agent", USER_AGENT)
        try {
            BufferedReader(InputStreamReader(urlConnection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    lineReader(line!!)
                }
            }
        } finally {
            urlConnection.disconnect()
        }
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36 Edg/88.0.705.50"
    }
}
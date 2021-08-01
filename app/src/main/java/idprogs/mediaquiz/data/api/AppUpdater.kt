package idprogs.mediaquiz.data.api

import idprogs.mediaquiz.data.api.model.AppVersion
import idprogs.mediaquiz.utility.PROGRAM_VERSION_INFO_URL
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AppUpdater: Updater {
    override suspend fun getCurrentAppVersion(): ResultWrapper<AppVersion> {
        return try {
            val url = URL(PROGRAM_VERSION_INFO_URL)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val text = urlConnection.inputStream.bufferedReader().readText()
                val data = JSONObject(text).getJSONObject("versions").getJSONObject("github")
                ResultWrapper.Success(AppVersion(version = data.getString("version"), versionCode = data.getInt("version_code"), apk = data.getString("apk")))
            } finally {
                urlConnection.disconnect()
            }
        } catch (throwable: Throwable) {
            ResultWrapper.Error
        }
    }
}
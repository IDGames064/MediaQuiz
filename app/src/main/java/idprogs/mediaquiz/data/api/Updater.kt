package idprogs.mediaquiz.data.api

import idprogs.mediaquiz.data.api.model.AppVersion

interface Updater {
    suspend fun getCurrentAppVersion(): ResultWrapper<AppVersion>
}
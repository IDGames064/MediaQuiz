package idprogs.mediaquiz

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import idprogs.mediaquiz.data.DataManager
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MainApp: Application(){

    @Inject lateinit var dataManager: DataManager

    override fun onCreate() {
        super.onCreate()
        runBlocking {
           dataManager.loadSettings()
        }
    }
}
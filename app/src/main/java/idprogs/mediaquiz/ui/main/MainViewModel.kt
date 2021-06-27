package idprogs.mediaquiz.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idprogs.mediaquiz.R
import idprogs.mediaquiz.data.DataManager
import idprogs.mediaquiz.data.api.ResultWrapper
import idprogs.mediaquiz.di.DispatcherProvider
import idprogs.mediaquiz.ui.base.BaseViewModel
import idprogs.mediaquiz.utility.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val dataManager: DataManager,
    private val dispatchers: DispatcherProvider
    ) : BaseViewModel() {
    private val TAG = "MainViewModel"

    val entryCount = MutableLiveData(0)

    private val mainChannel = Channel<Event>()
    val mainEvent = mainChannel.receiveAsFlow()

    private lateinit var oldLang: String
    var currentType: DataType = DATA_TYPE

    init {
       getEntryCount()
    }

    private fun getEntryCount() {
        viewModelScope.launch(dispatchers.io) {
            entryCount.postValue(dataManager.getEntryCount())
        }
    }

    private fun sendCommand(event: Event) {
        viewModelScope.launch { mainChannel.send(event) }
    }

    fun getMovieList() {
        viewModelScope.launch(dispatchers.io) {
            sendCommand(Event.Loading(true))
            val result = if (DATA_TYPE == DataType.DATA_ARTISTS) dataManager.loadArtistList() { sendCommand(Event.LoadingProgress(it)) }
                else dataManager.loadMovieList(LANGUAGE, MOVIES_VOTE_COUNT, MOVIES_VOTE_AVERAGE) { sendCommand(Event.LoadingProgress(it)) }
            withContext(dispatchers.main) {
                if (result is ResultWrapper.Success) entryCount.postValue(result.value)
                else sendCommand(Event.Error(R.string.error_loading_list))
            }
            sendCommand(Event.Loading(false))
        }
    }

    fun onSetPreferences() {
        viewModelScope.launch(dispatchers.io) {
            dataManager.saveSettings()
            if (oldLang != LANGUAGE || currentType != DATA_TYPE) sendCommand(Event.Restart)
        }

    }

    fun startQuiz() {
        sendCommand(Event.OpenQuizActivity)
    }
    fun showPreferences() {
        oldLang = LANGUAGE
        currentType = DATA_TYPE
        sendCommand(Event.ShowPreferences)
    }
    fun showInfo() {
        sendCommand(Event.ShowInfo)
    }

    sealed class Event {
        object Restart : Event()
        object OpenQuizActivity: Event()
        object ShowPreferences: Event()
        object ShowInfo: Event()
        data class Loading(val value: Boolean): Event()
        data class LoadingProgress(val progress: Int): Event()
        data class Error(val messageRes: Int): Event()
        object Empty: Event()
    }

}

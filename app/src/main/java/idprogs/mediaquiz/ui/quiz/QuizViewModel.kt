package idprogs.mediaquiz.ui.quiz


import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idprogs.mediaquiz.R
import idprogs.mediaquiz.data.DataManager
import idprogs.mediaquiz.data.api.ResultWrapper
import idprogs.mediaquiz.data.api.model.Artist
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.api.model.MusicVideo
import idprogs.mediaquiz.di.DispatcherProvider
import idprogs.mediaquiz.ui.base.BaseViewModel
import idprogs.mediaquiz.utility.*
import idprogs.mediaquiz.utility.CommonUtils.Companion.getSymbols
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.*
import javax.inject.Inject


enum class TimerState {
    STATE_RUNNING,
    STATE_PAUSED,
    STATE_FINISHED,
    STATE_INACTIVE,
    STATE_RESTARTED
}

@HiltViewModel
class QuizViewModel @Inject constructor (
    private val dataManager: DataManager,
    private val dispatchers: DispatcherProvider
    ) : BaseViewModel() {
    private val TAG = "QuizViewModel"
    private var count = 1

    val movieLiveData = MutableLiveData<Movie?>()
    val artistLiveData = MutableLiveData<Artist?>()
    val musicVideoLiveData = MutableLiveData<MusicVideo?>()
    val currentType: DataType = DATA_TYPE

    val countDown = MutableLiveData<Int>()
    val questionNumber = MutableLiveData<Int>()
    val pointCount = MutableLiveData<Int>()
    val questionAnswered = MutableLiveData<Boolean>()
    val answerOptions = MutableLiveData<List<String>?>()
    val error = MutableLiveData<Int?>()
    private var timer: CountDownTimer? = null
    val timerState = MutableLiveData<TimerState>()
    private var remainingTime = 0L
    private var pointsAdded = false
    private val testMovies: Queue<Int>
    private val testArtists: Queue<Pair<Int, Int>>

    private val quizChannel = Channel<Event>()
    val quizEvent = quizChannel.receiveAsFlow()


    init {
        timerState.value = TimerState.STATE_INACTIVE
        pointCount.value = 0
        testMovies = LinkedList(listOf())
        testArtists = LinkedList(listOf())
        getNextQuestion()
    }

    private fun sendCommand(event: Event) {
        viewModelScope.launch { quizChannel.send(event) }
    }

    private fun startCountdown(time: Long) {
        timerState.value = TimerState.STATE_RUNNING
        timer = object: CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDown.value = (millisUntilFinished/1000).toInt()
                if (timerState.value == TimerState.STATE_PAUSED) {
                    remainingTime = millisUntilFinished
                    cancel()
                }
            }
            override fun onFinish() {
                countDown.value = 0
                timerState.value = TimerState.STATE_FINISHED
            }
        }
        timer?.start()
    }

    private fun cancelCountdown() {
        timer?.cancel()
        timerState.value = TimerState.STATE_INACTIVE
    }

    fun restartCountdown() {
        if (questionAnswered.value == false) {
                timerState.value = TimerState.STATE_RESTARTED
                startCountdown(PLAYBACK_TIME.toLong() + 500)
            }
    }

    fun onVideoPrepared() {
        if (timerState.value == TimerState.STATE_FINISHED || timerState.value == TimerState.STATE_INACTIVE) restartCountdown()
    }

    fun onDestroy() {
        cancelCountdown()
        timer = null
    }

    fun onPause() {
        timerState.value = TimerState.STATE_PAUSED
    }

    fun onResume() {
        if (timerState.value == TimerState.STATE_PAUSED) {
            if (questionAnswered.value == false) startCountdown(remainingTime) else {
                cancelCountdown()
            }
        }
    }

    fun onVideoStart(index: Int) {
        musicVideoLiveData.postValue(artistLiveData.value!!.videos[index])
    }


    private fun correctAnswer() = if (currentType == DataType.DATA_ARTISTS) artistLiveData.value?.name?.getSymbols() else movieLiveData.value?.title?.getSymbols()

    fun onAnswerGot(answer: String) {
        val result = answer == correctAnswer()
        if (result && !pointsAdded) {
            pointCount.value?.let {a -> pointCount.value = a + 1 }
        }
        pointsAdded = true
        questionAnswered.value = result
        if (questionAnswered.value == true) cancelCountdown()
    }

    fun onAnswerChanged(answer: String) {
        val result = answer == correctAnswer()
        if (result && !pointsAdded) {
            pointCount.value?.let {a -> pointCount.value = a + 1 }
            pointsAdded = true
        }
        questionAnswered.value = result
        if (questionAnswered.value == true) cancelCountdown()
    }

    fun shareEntry() {
        val url = if (currentType == DataType.DATA_ARTISTS) musicVideoLiveData.value!!.url else TMDB_BASE_URL + DATA_TYPE.typeName + '/' + movieLiveData.value!!.id
        sendCommand(Event.ShareCurrentEntry(url))
    }


    fun nextQuestion() {
        sendCommand(Event.Vibrate)
        getNextQuestion()
    }

    private suspend fun getNextMovie() {
        val id = testMovies.poll()
        val movie = if (id != null) dataManager.getMovie(id, LANGUAGE) else {
            if (QUIZ_MODE == QuizMode.MODE_LETTER) dataManager.getRandomMovie(LANGUAGE, MAX_LENGTH_IN_LETTER_MODE) else dataManager.getRandomMovie(LANGUAGE)
        }
        if (movie is ResultWrapper.Success) {
            movieLiveData.postValue(movie.value)
            if (QUIZ_MODE == QuizMode.MODE_OPTIONS) {
                answerOptions.postValue(List(NUMBER_OF_OPTIONS) { "" } )
                val options = dataManager.getRandomMovieOptions(movie.value, NUMBER_OF_OPTIONS)
                if (options is ResultWrapper.Success) answerOptions.postValue(options.value) else error.postValue(R.string.error_loading_video)
            }
        } else error.postValue(R.string.error_loading_video)
    }

    private suspend fun getNextArtist() {
        val id = testArtists.poll()
        val artist = if (id != null) dataManager.getArtist(id.first, id.second) else dataManager.getArtist()
        if (artist is ResultWrapper.Success) {
            artistLiveData.postValue(artist.value)
            if (QUIZ_MODE == QuizMode.MODE_OPTIONS) {
                answerOptions.postValue(List(NUMBER_OF_OPTIONS) { "" } )
                val options = dataManager.getRandomArtistOptions(artist.value, NUMBER_OF_OPTIONS)
                if (options is ResultWrapper.Success) answerOptions.postValue(options.value) else error.postValue(R.string.error_loading_video)
            }
        } else error.postValue(R.string.error_loading_video)
    }

    private fun getNextQuestion() {
        timer?.cancel()
        viewModelScope.launch(dispatchers.io) {
            pointsAdded = false
            questionAnswered.postValue(false)
            questionNumber.postValue(count++)
            countDown.postValue(0)
            if (currentType == DataType.DATA_ARTISTS) getNextArtist() else getNextMovie()
        }
    }

    sealed class Event {
        object Vibrate : Event()
        data class ShareCurrentEntry(val link: String): Event()
        object Empty: Event()
    }

}
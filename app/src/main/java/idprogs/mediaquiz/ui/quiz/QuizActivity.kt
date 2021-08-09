package idprogs.mediaquiz.ui.quiz

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import idprogs.mediaquiz.BR
import idprogs.mediaquiz.R
import idprogs.mediaquiz.data.api.model.Artist
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.databinding.ActivityQuizBinding
import idprogs.mediaquiz.ui.base.BaseActivity
import idprogs.mediaquiz.ui.quiz.letterquiz.LetterQuizFragment
import idprogs.mediaquiz.ui.quiz.optionquiz.OptionQuizFragment
import idprogs.mediaquiz.utility.*
import idprogs.mediaquiz.utility.ytextractor.YouTubeExtractor
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class QuizActivity: BaseActivity(), Player.EventListener {
    private var currentIndex = 0
    private val TAG = "QuizActivity"
    companion object {
        fun newIntent(context: Context) = Intent(context, QuizActivity::class.java)
    }

    private val mViewModel: QuizViewModel by viewModels()
    lateinit var mBinding: ActivityQuizBinding
    @Inject
    lateinit var extractor: YouTubeExtractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_quiz)
        mBinding.lifecycleOwner = this
        mBinding.setVariable(BR.viewModel, mViewModel)
        mBinding.executePendingBindings()
        subscribeToLiveData()
        setupPlayer()
        adjustUIForScreenOrientation(resources.configuration.orientation)
        val fragment = if (QUIZ_MODE == QuizMode.MODE_OPTIONS) OptionQuizFragment() else LetterQuizFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fContainer, fragment).commitAllowingStateLoss()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        showInFullscreen()
    }


    override fun onPlaybackStateChanged(state: Int) {
        if (state == STATE_READY) {
            mBinding.progressBar.visibility = View.GONE
            mBinding.videoView.visibility = View.VISIBLE
            mViewModel.onVideoPrepared()
        }
    }


    override fun onPlayerError(error: ExoPlaybackException) {
        if (mViewModel.currentType == DataType.DATA_ARTISTS) playMusicVideo(mViewModel.artistLiveData.value!!, currentIndex, error)
        else playMovieTrailer(mViewModel.movieLiveData.value!!, currentIndex, error)
    }

    private fun setupPlayer() {
       val player = SimpleExoPlayer.Builder(this).build()
       player.addListener(this)
       mBinding.videoView.player = player
    }

    override fun onResume() {
        super.onResume()
        mViewModel.onResume()
    }

    override fun onPause() {
        mViewModel.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mBinding.videoView.player?.stop()
        mViewModel.onDestroy()
        super.onDestroy()
    }


    private fun playMovieTrailer(movie: Movie, index: Int, error: ExoPlaybackException? = null) {
        fun onPlayError(movie: Movie, index: Int) = playMovieTrailer(movie, index + 1)

        val key = CommonUtils.getTrailer(movie, index)
        Log.d(TAG, "playVideo: ${BASE_VIDEO_URL + key}")
        if (key.isNotEmpty()) {
            mBinding.fallback.setImageDrawable(null)
            mBinding.fallback.visibility = View.GONE
            lifecycleScope.launchWhenStarted {
               val items = extractor.extract(BASE_VIDEO_URL + key)?.ytFiles
               if (items != null) {
                   mBinding.progressBar.visibility = View.VISIBLE
                   var item = items[22] ?: items[18]
                   if (error != null) item = items[18]
                   val mediaItem = MediaItem.fromUri(Uri.parse(item?.url))
                   mBinding.videoView.player?.setMediaItem(mediaItem)
                   mBinding.videoView.player?.prepare()
               }
               else onPlayError(movie, index)
            }
        }
        else {
            Glide.with(this)
                .load(BASE_IMAGE_URL + mViewModel.movieLiveData.value?.backdrop_path)
                .into(mBinding.fallback)
            mBinding.fallback.visibility = View.VISIBLE
            mBinding.videoView.player?.prepare()
            mViewModel.onVideoPrepared()
        }
    }

    private fun playMusicVideo(artist: Artist, index: Int, error: ExoPlaybackException? = null) {
        fun onPlayError(artist: Artist, index: Int) {
            val nextIdx = if (index < artist.videos.size-1) index + 1 else 0
            if (nextIdx != artist.videoIndex) playMusicVideo(artist, nextIdx)
        }
        currentIndex = index
        mViewModel.onVideoStart(index)
        val url = artist.videos[index].url
        Log.d(TAG, "playing: $url  ${artist.videos[index].title}")
        lifecycleScope.launchWhenStarted {
            val items = extractor.extract(url)?.ytFiles
            if (items != null) {
                mBinding.progressBar.visibility = View.VISIBLE
                var item = items[22] ?: items[18]
                if (error != null) item = items[18]
                val mediaItem = MediaItem.fromUri(Uri.parse(item?.url))
                mBinding.videoView.player?.setMediaItem(mediaItem)
                mBinding.videoView.player?.prepare()
            }
            else onPlayError(artist, index)
        }

    }

    private fun prepare() = mBinding.videoView.apply {
        visibility = View.INVISIBLE
        player?.stop()
        player?.clearMediaItems()
    }

    private fun displayMovieQuestion(movie: Movie) {
        Log.d(TAG, "displayQuestion: ${movie.id}")
        prepare()
        playMovieTrailer(movie, 0)
    }

    private fun displayArtistQuestion(artist: Artist) {
        Log.d(TAG, "displayQuestion: ${artist.id}")
        prepare()
        if (artist.videoIndex < artist.videos.size) playMusicVideo(artist, artist.videoIndex)
    }


    private fun subscribeToLiveData() {
        mViewModel.movieLiveData.observe(this, {
            if (it != null) displayMovieQuestion(it)
        })
        mViewModel.artistLiveData.observe(this, {
            if (it != null) displayArtistQuestion(it)
        })

        mViewModel.timerState.observe(this, {
            Log.d(TAG, "timer state = $it")
            when (it) {
                TimerState.STATE_RUNNING, TimerState.STATE_INACTIVE -> mBinding.videoView.player?.play()
                TimerState.STATE_PAUSED, TimerState.STATE_FINISHED -> mBinding.videoView.player?.pause()
                TimerState.STATE_RESTARTED -> if (mViewModel.currentType == DataType.DATA_ARTISTS) mBinding.videoView.player?.seekTo(MUSIC_SEEK_TO) else mBinding.videoView.player?.seekTo(SEEK_TO)
                else -> { }
            }
        })
        mViewModel.questionAnswered.observe(this, {
            showInFullscreen()
            mBinding.btnNext.requestFocus()
        })

        mViewModel.error.observe(this, { if (it != null) showError(getString(it)) })
        lifecycleScope.launchWhenStarted {
            mViewModel.quizEvent.collect {
               when (it) {
                   is QuizViewModel.Event.ShareCurrentEntry -> shareCurrentEntry(it.link)
                   QuizViewModel.Event.Vibrate -> vibrate()
                   QuizViewModel.Event.Empty -> Unit
               }
            }
        }
    }

    private fun vibrate() {
        CommonUtils.vibratePhone(this)
    }

    private fun shareCurrentEntry(url: String) {
        if (url.isNotEmpty()) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            val title = mBinding.tvMovie.text
            if (mViewModel.currentType == DataType.DATA_ARTISTS) sendIntent.putExtra(Intent.EXTRA_TEXT,url)
              else sendIntent.putExtra(Intent.EXTRA_TEXT, "$title\n$url")
            sendIntent.type = "text/plain"
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun adjustUIForScreenOrientation(orientation: Int) {
        val isVertical = orientation == Configuration.ORIENTATION_PORTRAIT
        mBinding.llMain.background = if (mViewModel.currentType == DataType.DATA_ARTISTS) ResourcesCompat.getDrawable(resources, R.drawable.music_bg2,null) else ResourcesCompat.getDrawable(resources, R.drawable.movie_bg,null)
        mBinding.llMain.orientation = if (isVertical) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        (mBinding.flTop.layoutParams as LinearLayout.LayoutParams).apply {
            weight = if (isVertical) 0f else 3f
            width = if (isVertical) ViewGroup.LayoutParams.MATCH_PARENT else 0
        }
        (mBinding.flBottom.layoutParams as LinearLayout.LayoutParams).apply {
            weight = if (isVertical) 0f else 2f
            width = if (isVertical) ViewGroup.LayoutParams.MATCH_PARENT else 0
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustUIForScreenOrientation(newConfig.orientation)
    }
}

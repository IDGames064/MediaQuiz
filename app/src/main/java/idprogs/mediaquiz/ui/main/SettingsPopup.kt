package idprogs.mediaquiz.ui.main

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RadioGroup
import idprogs.mediaquiz.R
import idprogs.mediaquiz.databinding.PopupPreferencesBinding
import idprogs.mediaquiz.utility.*


class SettingsPopup(val context: Context) {
    private val dialog = Dialog(context)
    private var binding: PopupPreferencesBinding = PopupPreferencesBinding.inflate(LayoutInflater.from(context))
    private var dismissListener: (()->Unit)? = null

    private fun setVisibleSettingsSubPanel() {
        binding.llMovies.visibility = View.GONE
        binding.llSeries.visibility = View.GONE
        binding.llArtists.visibility = View.GONE
        when {
            binding.rbMovies.isChecked -> binding.llMovies.visibility = View.VISIBLE
            binding.rbSeries.isChecked -> binding.llSeries.visibility = View.VISIBLE
            binding.rbArtists.isChecked -> binding.llArtists.visibility = View.VISIBLE
        }
    }

    private fun getValues() {
        if (QUIZ_MODE == QuizMode.MODE_LETTER) binding.rbLetters.isChecked = true else binding.rbOptions.isChecked = true
        when (DATA_TYPE) {
            DataType.DATA_MOVIES -> binding.rbMovies.isChecked = true
            DataType.DATA_SERIES -> binding.rbSeries.isChecked = true
            DataType.DATA_ARTISTS -> binding.rbArtists.isChecked = true
        }
        if (LANGUAGE == "ru") binding.rbRussian.isChecked = true else binding.rbEnglish.isChecked = true
        binding.tvMovieVoteCount.text = context.getString(R.string.vote_count, MOVIES_VOTE_COUNT)
        binding.tvMovieVoteAverage.text = context.getString(R.string.vote_average, MOVIES_VOTE_AVERAGE)
        binding.sbMovieVoteCount.addOnChangeListener { _,value,_ ->  binding.tvMovieVoteCount.text = context.getString(R.string.vote_count, value.toInt())}
        binding.sbMovieVoteAverage.addOnChangeListener { _,value,_ ->  binding.tvMovieVoteAverage.text = context.getString(R.string.vote_average, value)}
        binding.sbMovieVoteCount.value = MOVIES_VOTE_COUNT+0f
        binding.sbMovieVoteAverage.value = MOVIES_VOTE_AVERAGE+0f

        binding.tvSeriesVoteCount.text = context.getString(R.string.vote_count, SERIES_VOTE_COUNT)
        binding.tvSeriesVoteAverage.text = context.getString(R.string.vote_average, SERIES_VOTE_AVERAGE)
        binding.sbSeriesVoteCount.addOnChangeListener { _,value,_ ->  binding.tvSeriesVoteCount.text = context.getString(R.string.vote_count, value.toInt())}
        binding.sbSeriesVoteAverage.addOnChangeListener { _,value,_ ->  binding.tvSeriesVoteAverage.text = context.getString(R.string.vote_average, value)}
        binding.sbSeriesVoteCount.value = SERIES_VOTE_COUNT+0f
        binding.sbSeriesVoteAverage.value = SERIES_VOTE_AVERAGE+0f

        binding.tvArtistCount.text = context.getString(R.string.artist_count, ARTIST_COUNT)
        binding.sbArtistCount.addOnChangeListener { _,value,_ ->  binding.tvArtistCount.text = context.getString(R.string.artist_count, value.toInt())}
        binding.sbArtistCount.value = ARTIST_COUNT+0f


        binding.rgDataType.setOnCheckedChangeListener { _, _ -> setVisibleSettingsSubPanel()}
    }

    private fun setValues() {
        QUIZ_MODE = if (binding.rbLetters.isChecked) QuizMode.MODE_LETTER else QuizMode.MODE_OPTIONS
        DATA_TYPE = when {
            binding.rbMovies.isChecked -> DataType.DATA_MOVIES
            binding.rbSeries.isChecked -> DataType.DATA_SERIES
            binding.rbArtists.isChecked -> DataType.DATA_ARTISTS
            else -> DataType.DATA_MOVIES
        }
        LANGUAGE = if (binding.rbRussian.isChecked) "ru" else "en"
        MOVIES_VOTE_COUNT = binding.sbMovieVoteCount.value.toInt()
        MOVIES_VOTE_AVERAGE = binding.sbMovieVoteAverage.value

        SERIES_VOTE_COUNT = binding.sbSeriesVoteCount.value.toInt()
        SERIES_VOTE_AVERAGE = binding.sbSeriesVoteAverage.value

        ARTIST_COUNT = binding.sbArtistCount.value.toInt()
    }

    init {
        dialog.setContentView(binding.root)
        val window = dialog.window
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val wlp: WindowManager.LayoutParams = window.attributes
            val width = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
            wlp.width = width
            window.attributes = wlp
        }

        getValues()
        dialog.setOnDismissListener {
            setValues()
            dismissListener?.invoke()
        }
    }

    fun popup() {
        dialog.show()
    }

    fun close() {
        dialog.hide()
    }

    fun onDismiss(listener: (() -> Unit)?) {
        this.dismissListener = listener
    }

}
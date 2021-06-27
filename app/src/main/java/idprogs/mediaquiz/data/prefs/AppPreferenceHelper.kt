package idprogs.mediaquiz.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import idprogs.mediaquiz.utility.*
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore("settings")


class AppPreferenceHelper @Inject constructor(@ApplicationContext appContext: Context): PreferenceHelper {
    private val prefDataType = stringPreferencesKey("PREF_DATA_TYPE")
    private val prefQuizMode = stringPreferencesKey("PREF_MODE")
    private val prefLanguage = stringPreferencesKey("PREF_LANGUAGE")
    private val prefMovieVoteCount = intPreferencesKey("PREF_MOVIE_VOTE_COUNT")
    private val prefMovieVoteAverage = floatPreferencesKey("PREF_MOVIE_VOTE_AVERAGE")
    private val prefSeriesVoteCount = intPreferencesKey("PREF_SERIES_VOTE_COUNT")
    private val prefSeriesVoteAverage = floatPreferencesKey("PREF_SERIES_VOTE_AVERAGE")
    private val prefArtistCount = intPreferencesKey("PREF_ARTIST_COUNT")

    private val settingsDataStore = appContext.dataStore

    override fun getDataType() = settingsDataStore.data.map { settings ->
        settings[prefDataType]?.let { DataType.valueOf(it) } ?: DATA_TYPE
    }

    override suspend fun setDataType(type: DataType) {
        settingsDataStore.edit { settings ->
            settings[prefDataType] = type.toString()
        }
    }

    override fun getQuizMode() = settingsDataStore.data.map { settings ->
        settings[prefQuizMode]?.let { QuizMode.valueOf(it) } ?: QUIZ_MODE
    }

    override suspend fun setQuizMode(mode: QuizMode) {
        settingsDataStore.edit { settings ->
            settings[prefQuizMode] = mode.toString()
        }
    }

    override fun getLanguage() = settingsDataStore.data.map { settings ->
        settings[prefLanguage] ?: LANGUAGE_DEFAULT
    }

    override suspend fun setLanguage(language: String) {
        settingsDataStore.edit { settings ->
            settings[prefLanguage] = language
        }
    }

    override fun getMovieVoteCount() = settingsDataStore.data.map { settings ->
        settings[prefMovieVoteCount] ?: MOVIES_VOTE_COUNT_DEFAULT
    }

    override suspend fun setMovieVoteCount(count: Int) {
        settingsDataStore.edit { settings ->
            settings[prefMovieVoteCount] = count
        }
    }

    override fun getMovieVoteAverage() = settingsDataStore.data.map { settings ->
        settings[prefMovieVoteAverage] ?: MOVIES_VOTE_AVERAGE_DEFAULT
    }

    override suspend fun setMovieVoteAverage(average: Float) {
        settingsDataStore.edit { settings ->
            settings[prefMovieVoteAverage] = average
        }
    }

    override fun getSeriesVoteCount() = settingsDataStore.data.map { settings ->
        settings[prefSeriesVoteCount] ?: SERIES_VOTE_COUNT_DEFAULT
    }

    override suspend fun setSeriesVoteCount(count: Int) {
        settingsDataStore.edit { settings ->
            settings[prefSeriesVoteCount] = count
        }
    }

    override fun getSeriesVoteAverage() = settingsDataStore.data.map { settings ->
        settings[prefSeriesVoteAverage] ?: SERIES_VOTE_AVERAGE_DEFAULT
    }

    override suspend fun setSeriesVoteAverage(average: Float) {
        settingsDataStore.edit { settings ->
            settings[prefSeriesVoteAverage] = average
        }
    }

    override fun getArtistCount() = settingsDataStore.data.map { settings ->
        settings[prefArtistCount] ?: ARTIST_COUNT_DEFAULT
    }

    override suspend fun setArtistCount(count: Int) {
        settingsDataStore.edit { settings ->
            settings[prefArtistCount] = count
        }
    }


}
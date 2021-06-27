package idprogs.mediaquiz.data

import android.util.Log
import idprogs.mediaquiz.data.api.ApiHelper
import idprogs.mediaquiz.data.api.ResultWrapper
import idprogs.mediaquiz.data.api.model.Artist
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.db.DbHelper
import idprogs.mediaquiz.data.db.model.MovieEntry
import idprogs.mediaquiz.data.prefs.PreferenceHelper
import idprogs.mediaquiz.utility.*
import idprogs.mediaquiz.utility.CommonUtils.Companion.getSymbols
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

class AppDataManager @Inject constructor(private val apiHelper: ApiHelper,
                                         private val dbHelper: DbHelper,
                                         private val preferenceHelper: PreferenceHelper): DataManager {
    override suspend fun getMovieList(language: String, vote_count: Int, vote_average: Float) = apiHelper.getMovieList(language, vote_count, vote_average)
    override suspend fun getEntryCount() = dbHelper.getCount(DATA_TYPE)
    override suspend fun loadMovieList(language: String, vote_count: Int, vote_average: Float, onProgress: (p: Int) -> Unit): ResultWrapper<Int> {
        val list = apiHelper.getMovieList(language, vote_count, vote_average) { onProgress.invoke(it)}
        return if (list is ResultWrapper.Success) {
            dbHelper.clearEntries(DATA_TYPE)
            dbHelper.addMovieEntries(list.value.map { MovieEntry(it.id, it.title, DATA_TYPE.id) })
            ResultWrapper.Success(dbHelper.getCount(DATA_TYPE))
        } else
            ResultWrapper.Error
    }

    override suspend fun getMovie(movieId: Int, language: String): ResultWrapper<Movie> {
        val movie = apiHelper.getMovie(movieId, language)
        return if (movie is ResultWrapper.Success) {
            val secondLang = if (language == "ru") "en" else "ru"
            val movieSecondLang = apiHelper.getMovie(movieId, secondLang)
            if (movieSecondLang is ResultWrapper.Success) {
                movie.value.videos.results.addAll(movieSecondLang.value.videos.results)
                movie
            } else
                ResultWrapper.Error
        } else ResultWrapper.Error
    }

    private suspend fun getMovieLight(movieId: Int, language: String): ResultWrapper<Movie> {
        val movie = apiHelper.getMovieLight(movieId, language)
        return if (movie is ResultWrapper.Success) movie else ResultWrapper.Error
    }

    override suspend fun getRandomMovie(language: String, maxTitleLength: Int): ResultWrapper<Movie> {
        var entry: MovieEntry
        do entry = dbHelper.getRandomMovieEntry() while (entry.title.getSymbols().length > maxTitleLength)
        return getMovie(entry.id, language)
    }
    override suspend fun getRandomMovieOptions(movie: Movie, count: Int): ResultWrapper<List<String>> {
        if (LANGUAGE == "en") {
            val idList = dbHelper.getRandomMovieOptions(movie, count, true)
            val result = mutableListOf<String>()
            for (id in idList) {
                val res = getMovieLight(id.toInt(), LANGUAGE)
                if (res is ResultWrapper.Success) result.add(res.value.title) else return ResultWrapper.Error
            }
            return ResultWrapper.Success(result)
        }
        return ResultWrapper.Success(dbHelper.getRandomMovieOptions(movie, count))
    }

    override suspend fun getArtistList(country: String) = apiHelper.getArtistList(country)

    override suspend fun loadArtistList(country: String, onProgress: (p: Int) -> Unit): ResultWrapper<Int> {
        val list = apiHelper.getArtistList(country) {onProgress.invoke(it)}
        return if (list is ResultWrapper.Success) {
            dbHelper.clearEntries(DATA_TYPE)
            dbHelper.addArtistEntries(list.value)
            ResultWrapper.Success(dbHelper.getCount(DATA_TYPE))
        } else
            ResultWrapper.Error
    }

    override suspend fun getArtist(id: Int, ids: Int): ResultWrapper<Artist> {
        val entry = if (id == -1) dbHelper.getRandomArtistEntry() else dbHelper.getArtistEntry(id)
        val videos = apiHelper.getMusicVideos(entry)
        return if (videos is ResultWrapper.Success) {
            val artist = Artist(entry.id, entry.name, entry.listeners, entry.mbid, entry.url, videos.value)
            if (artist.videos.isNotEmpty()) {
                if (ids >= 0 && ids <= artist.videos.size-1) artist.videoIndex = ids
                else artist.videoIndex = Random.nextInt(0, artist.videos.size)
            }
            //Log.d(TAG, "selected:\n$artist")
            ResultWrapper.Success(artist)
        } else {
            ResultWrapper.Error
        }
    }

    override suspend fun getRandomArtistOptions(artist: Artist, count: Int): ResultWrapper<List<String>> {
        return ResultWrapper.Success(dbHelper.getRandomArtistOptions(artist, count))
    }

    override suspend fun saveSettings() {
        preferenceHelper.setDataType(DATA_TYPE)
        preferenceHelper.setQuizMode(QUIZ_MODE)
        preferenceHelper.setLanguage(LANGUAGE)
        preferenceHelper.setMovieVoteCount(MOVIES_VOTE_COUNT)
        preferenceHelper.setMovieVoteAverage(MOVIES_VOTE_AVERAGE)
        preferenceHelper.setSeriesVoteCount(SERIES_VOTE_COUNT)
        preferenceHelper.setSeriesVoteAverage(SERIES_VOTE_AVERAGE)
        preferenceHelper.setArtistCount(ARTIST_COUNT)
    }

    override suspend fun loadSettings() {
        DATA_TYPE = preferenceHelper.getDataType().first()
        QUIZ_MODE = preferenceHelper.getQuizMode().first()
        LANGUAGE = preferenceHelper.getLanguage().first()
        MOVIES_VOTE_COUNT = preferenceHelper.getMovieVoteCount().first()
        MOVIES_VOTE_AVERAGE = preferenceHelper.getMovieVoteAverage().first()
        SERIES_VOTE_COUNT = preferenceHelper.getSeriesVoteCount().first()
        SERIES_VOTE_AVERAGE = preferenceHelper.getSeriesVoteAverage().first()
        ARTIST_COUNT = preferenceHelper.getArtistCount().first()
    }
}

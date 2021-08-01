package idprogs.mediaquiz.data

import idprogs.mediaquiz.data.api.ResultWrapper
import idprogs.mediaquiz.data.api.model.AppVersion
import idprogs.mediaquiz.data.api.model.Artist
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.data.db.model.MovieEntry
import idprogs.mediaquiz.utility.LASTFM_DEFAULT_COUNTRY

interface DataManager {
    suspend fun getMovie(movieId: Int, language: String) : ResultWrapper<Movie>
    suspend fun getMovieList(language: String, vote_count: Int, vote_average: Float) : ResultWrapper<List<MovieEntry>>
    suspend fun getEntryCount() : Int
    suspend fun loadMovieList(language: String, vote_count: Int, vote_average: Float, onProgress: (p: Int) -> Unit = {}) : ResultWrapper<Int>
    suspend fun getRandomMovie(language: String, maxTitleLength: Int = 100) : ResultWrapper<Movie>
    suspend fun getRandomMovieOptions(movie: Movie, count: Int): ResultWrapper<List<String>>

    suspend fun getArtistList(country: String = LASTFM_DEFAULT_COUNTRY): ResultWrapper<List<ArtistEntry>>
    suspend fun loadArtistList(country: String = LASTFM_DEFAULT_COUNTRY, onProgress: (p: Int) -> Unit = {}): ResultWrapper<Int>
    suspend fun getArtist(id: Int = -1, ids: Int = -1): ResultWrapper<Artist>
    suspend fun getRandomArtistOptions(artist: Artist, count: Int): ResultWrapper<List<String>>

    suspend fun loadSettings()
    suspend fun saveSettings()

    suspend fun getCurrentAppVersion(): ResultWrapper<AppVersion>
}
package idprogs.mediaquiz.data.api

import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.api.model.MusicVideo
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.data.db.model.MovieEntry

interface ApiHelper {
    suspend fun getMovie(movieId: Int, language: String) : ResultWrapper<Movie>
    suspend fun getMovieLight(movieId: Int, language: String) : ResultWrapper<Movie>
    suspend fun getMovieList(language: String, vote_count: Int, vote_average: Float, onProgress: (p: Int) -> Unit = {}) : ResultWrapper<List<MovieEntry>>

    suspend fun getArtistList(country: String, onProgress: (p: Int) -> Unit = {}): ResultWrapper<List<ArtistEntry>>
    suspend fun getMusicVideos(artist: ArtistEntry): ResultWrapper<List<MusicVideo>>

}
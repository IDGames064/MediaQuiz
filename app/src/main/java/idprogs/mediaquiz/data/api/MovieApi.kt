package idprogs.mediaquiz.data.api

import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.api.model.MovieResponse
import idprogs.mediaquiz.utility.DATA_TYPE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {
    @GET("discover/{type}")
    suspend fun getMovieList(@Path("type") typeName: String = DATA_TYPE.typeName,
                             @Query("language") language: String,
                             @Query("vote_count.gte") vote_count: Int,
                             @Query("vote_average.gte") vote_average: Float,
                             @Query("page") page: Int = 1
                            ): MovieResponse

    @GET("{type}/{movieId}")
    suspend fun getMovie(@Path("type") typeName: String = DATA_TYPE.typeName,
                         @Path("movieId") movieId: Int,
                         @Query("language") language: String,
                         @Query("append_to_response") append: String = "videos",
                        ): Movie

    @GET("{type}/{movieId}")
    suspend fun getMovieLight(@Path("type") typeName: String = DATA_TYPE.typeName,
                         @Path("movieId") movieId: Int,
                         @Query("language") language: String
    ): Movie
}
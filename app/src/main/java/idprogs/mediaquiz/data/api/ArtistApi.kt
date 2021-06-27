package idprogs.mediaquiz.data.api

import idprogs.mediaquiz.data.api.model.TopArtistsResponse
import idprogs.mediaquiz.utility.LASTFM_DEFAULT_COUNTRY
import idprogs.mediaquiz.utility.PAGE_SIZE
import retrofit2.http.GET
import retrofit2.http.Query

interface ArtistApi {
    @GET("?method=geo.gettopartists")
    suspend fun getArtistList(@Query("country") country: String = LASTFM_DEFAULT_COUNTRY,
                              @Query("page") page: Int = 1,
                              @Query("limit") limit: Int = PAGE_SIZE,
    ): TopArtistsResponse
}
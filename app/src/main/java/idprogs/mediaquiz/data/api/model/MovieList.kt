package idprogs.mediaquiz.data.api.model

import com.google.gson.annotations.SerializedName
import idprogs.mediaquiz.data.db.model.MovieEntry

data class MovieResponse (
    @SerializedName("page") val page : Int,
    @SerializedName("total_results") val total_results : Int,
    @SerializedName("total_pages") val total_pages : Int,
    @SerializedName("results") val results : List<MovieEntry>
)
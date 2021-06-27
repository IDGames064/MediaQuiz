package idprogs.mediaquiz.data.api.model

import com.google.gson.annotations.SerializedName

data class Video (
    @SerializedName("id") val id : String,
    @SerializedName("iso_639_1") val iso_639_1 : String,
    @SerializedName("iso_3166_1") val iso_3166_1 : String,
    @SerializedName("key") val key : String,
    @SerializedName("name") val name : String,
    @SerializedName("site") val site : String,
    @SerializedName("size") val size : Int,
    @SerializedName("type") val type : String
)

data class Videos (
    @SerializedName("results") val results : MutableList<Video>
)

data class Genre (
    @SerializedName("id") val id : Int,
    @SerializedName("name") val name : String
)

data class Movie (
        @SerializedName("popularity") val popularity : Double,
        @SerializedName("id") val id : Int,
        @SerializedName("imdb_id") val imdb_id : String = "",
        @SerializedName("genres") val genres : List<Genre>,
        @SerializedName("video") val video : Boolean,
        @SerializedName("vote_count") val vote_count : Int,
        @SerializedName("vote_average") val vote_average : Double,
        @SerializedName("title", alternate = ["name"]) val title : String,
        @SerializedName("tagline") val tagline : String,
        @SerializedName("release_date", alternate = ["first_air_date"]) val release_date : String,
        @SerializedName("number_of_seasons") val number_of_seasons : Int = 0,
        @SerializedName("original_language") val original_language : String,
        @SerializedName("original_title") val original_title : String,
        @SerializedName("backdrop_path") val backdrop_path : String,
        @SerializedName("adult") val adult : Boolean,
        @SerializedName("overview") val overview : String,
        @SerializedName("poster_path") val poster_path : String,
        @SerializedName("videos") val videos : Videos
) {
    fun getYear(): String = release_date.substringBefore("-")
    fun getGenresString(): String = genres.joinToString { it.name }
    fun getPercent(): String = "${(vote_average*10).toInt()}%"
}



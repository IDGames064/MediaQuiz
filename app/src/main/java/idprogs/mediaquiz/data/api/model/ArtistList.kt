package idprogs.mediaquiz.data.api.model

import com.google.gson.annotations.SerializedName
import idprogs.mediaquiz.data.db.model.ArtistEntry

data class TopArtistsResponse (
    @SerializedName("topartists") val topArtists : TopArtists
)

data class TopArtists (
    @SerializedName("artist") val artist : List<ArtistEntry>,
    @SerializedName("@attr") val attr : Attr
)

data class Attr (
    @SerializedName("country") val country : String,
    @SerializedName("page") val page : Int,
    @SerializedName("perPage") val perPage : Int,
    @SerializedName("totalPages") val totalPages : Int,
    @SerializedName("total") val total : Int
)

data class MusicVideo (
    @SerializedName("title") val title : String,
    @SerializedName("viewCount") val viewCount : Int,
    @SerializedName("url") val url : String,
){
    override fun toString(): String = "$title ($viewCount) $url\n"
}

data class Artist (
    @SerializedName("id") val id : Int,
    @SerializedName("name") val name : String,
    @SerializedName("listeners") val listeners : Int,
    @SerializedName("mbid") val mbid : String,
    @SerializedName("url") val url : String,
    @SerializedName("videos") val videos : List<MusicVideo>
){
    var videoIndex: Int = 0
    override fun toString(): String = "id = $id\nname = $name\nlisteners = $listeners\nurl = $url\nmbid = $mbid\nvideos:\n$videos"
}
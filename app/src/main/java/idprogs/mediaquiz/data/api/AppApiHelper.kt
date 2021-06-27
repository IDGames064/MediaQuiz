package idprogs.mediaquiz.data.api

import androidx.core.text.HtmlCompat
import idprogs.mediaquiz.data.api.model.MusicVideo
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.data.db.model.MovieEntry
import idprogs.mediaquiz.utility.ARTIST_COUNT
import idprogs.mediaquiz.utility.ARTIST_SUFFIX
import idprogs.mediaquiz.utility.PAGE_SIZE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import javax.inject.Inject


class AppApiHelper @Inject constructor(private val movieApi : MovieApi, private val artistApi : ArtistApi): ApiHelper {

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> T,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall())
            } catch (throwable: Throwable) {
                ResultWrapper.Error
            }
        }
    }

    override suspend fun getMovie(movieId: Int, language: String) = safeApiCall({movieApi.getMovie(movieId = movieId, language = language)})

    override suspend fun getMovieLight(movieId: Int, language: String) = safeApiCall({movieApi.getMovieLight(movieId = movieId, language = language)})

    override suspend fun getMovieList(language: String, vote_count: Int, vote_average: Float, onProgress: (p: Int) -> Unit): ResultWrapper<List<MovieEntry>>{
        val lang = "ru"  // !!!!
        val list = ArrayList<MovieEntry>()
        val response = safeApiCall({movieApi.getMovieList(language = lang, vote_count = vote_count, vote_average = vote_average, page = 1)})
        if (response is ResultWrapper.Success) {
            list.addAll(response.value.results)
            onProgress.invoke((1/response.value.total_pages.toFloat()*100).toInt())
            for (nPage in 2..response.value.total_pages) {
                val res = safeApiCall({movieApi.getMovieList(language = lang, vote_count = vote_count, vote_average = vote_average, page = nPage)})
                if (res is ResultWrapper.Success) list.addAll(res.value.results) else return ResultWrapper.Error
                onProgress.invoke((nPage/response.value.total_pages.toFloat()*100).toInt())
            }
            return ResultWrapper.Success(list)
        } else
            return ResultWrapper.Error
    }

    override suspend fun getArtistList(country: String, onProgress: (p: Int) -> Unit): ResultWrapper<List<ArtistEntry>>{
        val list = ArrayList<ArtistEntry>()
        val response = safeApiCall({ artistApi.getArtistList(page = 1) })
        val nPages = ARTIST_COUNT / PAGE_SIZE
        if (response is ResultWrapper.Success) {
            list.addAll(response.value.topArtists.artist)
            onProgress.invoke((1 / nPages.toFloat() * 100).toInt())
            for (nPage in 2..nPages) {
                val res = safeApiCall({ artistApi.getArtistList(page = nPage) })
                if (res is ResultWrapper.Success) list.addAll(res.value.topArtists.artist) else return ResultWrapper.Error
                onProgress.invoke((nPage / nPages.toFloat() * 100).toInt())
            }
            return ResultWrapper.Success(list)
        } else
            return ResultWrapper.Error

    }

    override suspend fun getMusicVideos(artist: ArtistEntry): ResultWrapper<List<MusicVideo>> {
        return safeApiCall({
            val videoList = ArrayList<MusicVideo>()
            val url = URL(artist.url + ARTIST_SUFFIX)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val text = urlConnection.inputStream.bufferedReader().readText()
                val pattern =
                    Pattern.compile("data-youtube-url=\"(.*?)\"[\\S\\s]*?data-track-name=\"(.*?)\"[\\S\\s]*?<span class=\"chartlist-count-bar-value\">[\\S\\s]*?([\\d,]*) <")
                val matcher = pattern.matcher(text)
                var topCount = 0
                while (matcher.find()) {
                    val count = if (matcher.group(3)!!.isNotBlank()) matcher.group(3)!!.replace(",", "")
                        .toInt() else 1
                    if (topCount == 0) topCount = count
                    if ((topCount / count < 3.0f) || (videoList.size < 5)) {
                        val name = HtmlCompat.fromHtml(matcher.group(2)!!, 0).toString()
                        val video = MusicVideo(name, count, matcher.group(1)!!)
                        videoList.add(video)
                    }
                }
            } finally {
                urlConnection.disconnect()
            }
            videoList
        })
    }

}
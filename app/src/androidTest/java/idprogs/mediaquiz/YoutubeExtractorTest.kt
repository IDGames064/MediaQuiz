package idprogs.mediaquiz

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import idprogs.mediaquiz.di.DispatcherProvider
import idprogs.mediaquiz.utility.ytextractor.HttpClient
import idprogs.mediaquiz.utility.ytextractor.VideoMeta
import idprogs.mediaquiz.utility.ytextractor.YouTubeExtractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
object TestDispatcherProvider: DispatcherProvider {
    override val main: CoroutineDispatcher
        get() = TestCoroutineDispatcher()
    override val io: CoroutineDispatcher
        get() = TestCoroutineDispatcher()
    override val default: CoroutineDispatcher
        get() = TestCoroutineDispatcher()
    override val unconfined: CoroutineDispatcher
        get() = TestCoroutineDispatcher()
}

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class YoutubeExtractorTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val httpClient = HttpClient()

    @Test
    fun test_video1() = runBlocking {
        val extractor = YouTubeExtractor(context, httpClient, TestDispatcherProvider)
        val result = extractor.extract("https://www.youtube.com/watch?v=LZl69yk5lEY")
        assertThat(result?.ytFiles).isNotNull()
        assertThat(result?.videoMeta).isEqualTo(VideoMeta(videoId="LZl69yk5lEY", title="The Mask (1994) Official Trailer - Jim Carrey Movie"))
    }

    @Test
    fun test_video2() = runBlocking {
        val extractor = YouTubeExtractor(context, httpClient, TestDispatcherProvider)
        val result = extractor.extract("https://www.youtube.com/watch?v=zBfs0mPiO3M")
        assertThat(result?.ytFiles).isNotNull()
        assertThat(result?.videoMeta).isEqualTo(VideoMeta(videoId="zBfs0mPiO3M", title="Leftfield - Release The Pressure"))
    }

    @Test
    fun test_video3() = runBlocking {
        val extractor = YouTubeExtractor(context, httpClient, TestDispatcherProvider)
        val result = extractor.extract("https://www.youtube.com/watch?v=GJ7sR5VG7_U")
        assertThat(result?.ytFiles).isNull()
    }

    @Test
    fun test_video4() = runBlocking {
        val extractor = YouTubeExtractor(context, httpClient, TestDispatcherProvider)
        val result = extractor.extract("https://www.youtube.com/watch?v=gh2lkb7QDUk")
        assertThat(result?.ytFiles).isNotNull()
        assertThat(result?.videoMeta).isEqualTo(VideoMeta(videoId="gh2lkb7QDUk", title="Epic Score   Creator Of Worlds Epic Action & Adventure 8"))
    }

    @Test
    fun test_video5() = runBlocking {
        val extractor = YouTubeExtractor(context, httpClient, TestDispatcherProvider)
        val result = extractor.extract("https://www.youtube.com/watch?v=VyS9GKNUsWI")
        assertThat(result?.ytFiles).isNotNull()
        assertThat(result?.videoMeta).isEqualTo(VideoMeta(videoId="VyS9GKNUsWI", title="Браво \"Дорога в облака\""))
    }



}
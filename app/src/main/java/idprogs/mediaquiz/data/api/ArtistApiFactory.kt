package idprogs.mediaquiz.data.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import idprogs.mediaquiz.utility.LASTFM_API_BASE_URL
import idprogs.mediaquiz.utility.LASTFM_API_KEY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ArtistApiFactory {
    private val logInterceptor = run {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.apply {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        }
    }


    private val authInterceptor = Interceptor {chain->
        val newUrl = chain.request().url
            .newBuilder()
            .addQueryParameter("api_key", LASTFM_API_KEY)
            .addQueryParameter("format", "json")
            .build()
        val newRequest = chain.request()
            .newBuilder()
            .url(newUrl)
            .build()
        chain.proceed(newRequest)
    }

    private val lastFmClient = OkHttpClient().newBuilder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logInterceptor)
        .build()

    private fun retrofit() : Retrofit = Retrofit.Builder()
        .client(lastFmClient)
        .baseUrl(LASTFM_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val artistApi : ArtistApi = retrofit().create(ArtistApi::class.java)
}
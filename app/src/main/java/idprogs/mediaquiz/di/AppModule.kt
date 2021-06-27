package idprogs.mediaquiz.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import idprogs.mediaquiz.data.AppDataManager
import idprogs.mediaquiz.data.DataManager
import idprogs.mediaquiz.data.api.*
import idprogs.mediaquiz.data.db.AppDatabase
import idprogs.mediaquiz.data.db.AppDbHelper
import idprogs.mediaquiz.data.db.DbHelper
import idprogs.mediaquiz.data.prefs.AppPreferenceHelper
import idprogs.mediaquiz.data.prefs.PreferenceHelper
import idprogs.mediaquiz.utility.DB_NAME
import idprogs.mediaquiz.utility.ytextractor.HttpClient
import idprogs.mediaquiz.utility.ytextractor.YouTubeExtractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDbHelper(appDbHelper: AppDbHelper): DbHelper = appDbHelper

    @Provides
    @Singleton
    fun provideApiHelper(appApiHelper: AppApiHelper): ApiHelper = appApiHelper

    @Provides
    @Singleton
    fun provideMovieApi(): MovieApi =  MovieApiFactory.movieApi

    @Provides
    @Singleton
    fun provideArtistApi(): ArtistApi =  ArtistApiFactory.artistApi

    @Provides
    @Singleton
    fun providePreferenceHelper(appPreferenceHelper: AppPreferenceHelper): PreferenceHelper = appPreferenceHelper

    @Provides
    @Singleton
    fun provideDataManager(appDataManager: AppDataManager): DataManager = appDataManager

    @Provides
    @Singleton
    fun provideHttpClient() = HttpClient

    @Provides
    @Singleton
    fun provideYoutubeExtractor(@ApplicationContext context: Context, http: HttpClient, dispatchers: DispatcherProvider) = YouTubeExtractor(context, http, dispatchers)


    @Singleton
    @Provides
    fun provideDispatchers(): DispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher
            get() = Dispatchers.Main
        override val io: CoroutineDispatcher
            get() = Dispatchers.IO
        override val default: CoroutineDispatcher
            get() = Dispatchers.Default
        override val unconfined: CoroutineDispatcher
            get() = Dispatchers.Unconfined
    }
}
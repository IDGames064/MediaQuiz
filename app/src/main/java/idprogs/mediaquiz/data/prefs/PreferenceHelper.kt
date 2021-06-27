package idprogs.mediaquiz.data.prefs

import idprogs.mediaquiz.utility.DataType
import idprogs.mediaquiz.utility.QuizMode
import kotlinx.coroutines.flow.Flow

interface PreferenceHelper {
  fun getDataType(): Flow<DataType>
  suspend fun setDataType(type: DataType)
  fun getQuizMode(): Flow<QuizMode>
  suspend fun setQuizMode(mode: QuizMode)
  fun getLanguage(): Flow<String>
  suspend fun setLanguage(language: String)
  fun getMovieVoteCount(): Flow<Int>
  suspend fun setMovieVoteCount(count: Int)
  fun getMovieVoteAverage(): Flow<Float>
  suspend fun setMovieVoteAverage(average: Float)
  fun getSeriesVoteCount(): Flow<Int>
  suspend fun setSeriesVoteCount(count: Int)
  fun getSeriesVoteAverage(): Flow<Float>
  suspend fun setSeriesVoteAverage(average: Float)
  fun getArtistCount(): Flow<Int>
  suspend fun setArtistCount(count: Int)
}
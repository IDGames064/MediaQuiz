package idprogs.mediaquiz.data.db

import idprogs.mediaquiz.data.api.model.Artist
import idprogs.mediaquiz.data.api.model.Movie
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.data.db.model.MovieEntry
import idprogs.mediaquiz.utility.CommonUtils.Companion.isCyrillic
import idprogs.mediaquiz.utility.DataType
import javax.inject.Inject

class AppDbHelper @Inject constructor(private val appDatabase : AppDatabase): DbHelper {
    override fun getCount(type: DataType) = when (type) {
        DataType.DATA_MOVIES -> appDatabase.movieEntryDao().getCount()
        DataType.DATA_SERIES -> appDatabase.movieEntryDao().getCount()
        DataType.DATA_ARTISTS -> appDatabase.artistEntryDao().getCount()
    }
    override fun getRandomMovieEntry() = appDatabase.movieEntryDao().getRandomEntry()
    override fun clearEntries(type: DataType) = when (type) {
        DataType.DATA_MOVIES -> appDatabase.movieEntryDao().clear()
        DataType.DATA_SERIES -> appDatabase.movieEntryDao().clear()
        DataType.DATA_ARTISTS -> appDatabase.artistEntryDao().clear()
    }
    override fun addMovieEntries(entries: List<MovieEntry>) = appDatabase.movieEntryDao().insertAll(entries)
    override fun getRandomMovieOptions(movie: Movie, count: Int, onlyId: Boolean): List<String> {
        var result = mutableListOf<String>()

        result.add(if (onlyId) movie.id.toString() else movie.title)
        while (result.size < count) {
            result.add(if (onlyId) appDatabase.movieEntryDao().getRandomEntry().id.toString() else appDatabase.movieEntryDao().getRandomEntry().title)
            result = result.distinct().toMutableList()
        }
        result.shuffle()
        return result
    }
    override fun addArtistEntries(entries: List<ArtistEntry>) = appDatabase.artistEntryDao().insertAll(entries)
    override fun getRandomArtistEntry() = appDatabase.artistEntryDao().getRandomEntry()
    override fun getArtistEntry(entryId: Int) = appDatabase.artistEntryDao().getEntry(entryId)
    override fun getRandomArtistOptions(artist: Artist, count: Int): List<String> {
        var result = mutableListOf<String>()
        val isCyrillic = artist.name.isCyrillic()
        result.add(artist.name)
        while (result.size < count) {
            var entry: ArtistEntry
            do {
                entry = appDatabase.artistEntryDao().getRandomEntry()
            } while (entry.name.isCyrillic() != isCyrillic)
            result.add(entry.name)
            result = result.distinct().toMutableList()
        }
        result.shuffle()
        return result
    }
}
package idprogs.mediaquiz.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.data.db.model.MovieEntry

@Database(entities = [MovieEntry::class, ArtistEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieEntryDao(): MovieEntryDao
    abstract fun artistEntryDao(): ArtistEntryDao
}
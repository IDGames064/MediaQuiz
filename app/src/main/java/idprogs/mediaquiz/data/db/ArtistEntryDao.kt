package idprogs.mediaquiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import idprogs.mediaquiz.data.db.model.ArtistEntry
import idprogs.mediaquiz.utility.ARTIST_EXCEPTIONS

@Dao
interface ArtistEntryDao {

    @Query("SELECT * FROM artists")
    fun getAll(): List<ArtistEntry>

    @Query("SELECT count(*) FROM artists")
    fun getCount(): Int

    @Query("SELECT * FROM artists WHERE mbid not in (:exceptions) ORDER BY RANDOM() LIMIT 1")
    fun getRandomEntry(exceptions: List<String> = ARTIST_EXCEPTIONS): ArtistEntry

    @Query("SELECT * FROM artists where id = :entryId LIMIT 1 ")
    fun getEntry(entryId: Int): ArtistEntry

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<ArtistEntry>)

    @Query("DELETE FROM artists")
    fun clear()
}
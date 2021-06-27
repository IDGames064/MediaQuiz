package idprogs.mediaquiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import idprogs.mediaquiz.data.db.model.MovieEntry
import idprogs.mediaquiz.utility.DATA_TYPE

@Dao
interface MovieEntryDao {
    @Query("SELECT count(*) FROM movies where type = :type")
    fun getCount(type: Int = DATA_TYPE.id): Int

    @Query("SELECT * FROM movies where type = :type ORDER BY RANDOM() LIMIT 1")
    fun getRandomEntry(type: Int = DATA_TYPE.id): MovieEntry

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<MovieEntry>)

    @Query("DELETE FROM movies where type = :type")
    fun clear(type: Int = DATA_TYPE.id)
}

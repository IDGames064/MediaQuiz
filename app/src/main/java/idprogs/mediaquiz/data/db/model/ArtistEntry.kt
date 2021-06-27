package idprogs.mediaquiz.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artists", indices = [Index(value = ["name"], unique = true)])
data class ArtistEntry(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "listeners") val listeners: Int,
    @ColumnInfo(name = "mbid") val mbid: String,
    @ColumnInfo(name = "url") val url: String
)
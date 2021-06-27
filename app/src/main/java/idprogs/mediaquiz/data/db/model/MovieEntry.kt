package idprogs.mediaquiz.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import idprogs.mediaquiz.utility.DATA_TYPE

@Entity (tableName = "movies")
data class MovieEntry(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "title")
        @SerializedName("title", alternate = ["name"])
        val title: String,
        var type: Int
)
package com.mahdi155000.clof_android.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    @ColumnInfo(defaultValue = "0")
    val createdAt: Long = System.currentTimeMillis(),

    val genre: String = "",

    val isSeries: Boolean = false,

    val season: Int = 0,

    val episode: Int = 0,

    val watched: Boolean = false,

    val collection: String = CollectionNames.MAIN,

    val inTrash: Boolean = false,

    val notes: String? = null
)
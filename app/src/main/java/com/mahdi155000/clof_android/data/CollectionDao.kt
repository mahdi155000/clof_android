package com.mahdi155000.clof_android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT name FROM collections ORDER BY name COLLATE NOCASE")
    fun observeCollections(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Query("DELETE FROM collections WHERE name = :name")
    suspend fun deleteCollectionRow(name: String)

    @Query("UPDATE collections SET name = :newName WHERE name = :oldName")
    suspend fun updateCollectionName(oldName: String, newName: String)

    @Query("UPDATE movies SET collection = :newName WHERE collection = :oldName")
    suspend fun moveMoviesToCollection(oldName: String, newName: String)

    @Transaction
    suspend fun renameCollection(oldName: String, newName: String): Boolean {
        if (CollectionNames.isReserved(oldName) ||
            CollectionNames.isReserved(newName) ||
            oldName == newName
        ) {
            return false
        }
        if (insertCollection(CollectionEntity(newName)) == -1L) return false

        moveMoviesToCollection(oldName, newName)
        deleteCollectionRow(oldName)
        return true
    }

    @Transaction
    suspend fun removeCollection(name: String): Boolean {
        if (CollectionNames.isReserved(name)) return false

        moveMoviesToCollection(name, CollectionNames.MAIN)
        deleteCollectionRow(name)
        return true
    }
}

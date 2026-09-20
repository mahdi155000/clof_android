package com.mahdi155000.clof_android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    @Query("SELECT name FROM genres ORDER BY name COLLATE NOCASE")
    fun observeGenres(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenre(genre: GenreEntity): Long

    @Query("DELETE FROM genres WHERE name = :name")
    suspend fun deleteGenreRow(name: String)

    @Query("UPDATE genres SET name = :newName WHERE name = :oldName")
    suspend fun updateGenreName(oldName: String, newName: String)

    @Query("UPDATE movies SET genre = :newName WHERE genre = :oldName")
    suspend fun updateMovieGenres(oldName: String, newName: String)

    @Query("UPDATE movies SET genre = '' WHERE genre = :name")
    suspend fun clearMovieGenres(name: String)

    @Transaction
    suspend fun renameGenre(oldName: String, newName: String): Boolean {
        if (oldName == newName || insertGenre(GenreEntity(newName)) == -1L) return false

        updateMovieGenres(oldName, newName)
        deleteGenreRow(oldName)
        return true
    }

    @Transaction
    suspend fun removeGenre(name: String): Boolean {
        clearMovieGenres(name)
        deleteGenreRow(name)
        return true
    }
}

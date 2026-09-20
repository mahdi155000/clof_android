package com.mahdi155000.clof_android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    @Query("SELECT * FROM movies")
    suspend fun getMovies(): List<MovieEntity>

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Transaction
    suspend fun renameGenre(oldName: String, newName: String): Boolean {
        if (oldName == newName || insertGenre(GenreEntity(newName)) == -1L) return false

        getMovies()
            .filter { movie -> oldName in movie.genre.split(",").map { it.trim() } }
            .forEach { movie ->
                val updatedGenres = movie.genre
                    .split(",")
                    .map { it.trim() }
                    .map { if (it == oldName) newName else it }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(", ")
                updateMovie(movie.copy(genre = updatedGenres))
            }
        deleteGenreRow(oldName)
        return true
    }

    @Transaction
    suspend fun removeGenre(name: String): Boolean {
        getMovies()
            .filter { movie -> name in movie.genre.split(",").map { it.trim() } }
            .forEach { movie ->
                val updatedGenres = movie.genre
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && it != name }
                    .distinct()
                    .joinToString(", ")
                updateMovie(movie.copy(genre = updatedGenres))
            }
        deleteGenreRow(name)
        return true
    }
}

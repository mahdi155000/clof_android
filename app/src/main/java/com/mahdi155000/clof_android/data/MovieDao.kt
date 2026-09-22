package com.mahdi155000.clof_android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies WHERE inTrash = 0 ORDER BY id")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE inTrash = 1 ORDER BY id DESC")
    fun getTrashMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE collection = :collection ORDER BY id")
    fun getMoviesByCollection(collection: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id")
    fun observeMovie(id: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovie(id: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE LOWER(TRIM(title)) = LOWER(TRIM(:title)) LIMIT 1")
    suspend fun getMovieByTitle(title: String): MovieEntity?

    @Query("""
        SELECT * FROM movies
        WHERE LOWER(TRIM(title)) = LOWER(TRIM(:title)) AND id != :id
        LIMIT 1
    """)
    suspend fun getMovieByTitleExcludingId(title: String, id: Int): MovieEntity?

    @Insert
    suspend fun insertMovie(movie: MovieEntity)

    @Transaction
    suspend fun insertMovieIfMissing(movie: MovieEntity): Boolean {
        if (getMovieByTitle(movie.title) != null) {
            return false
        }
        insertMovie(movie.copy(title = normalizeMovieTitle(movie.title)))
        return true
    }

    @Transaction
    suspend fun insertMissingMovies(movies: List<MovieEntity>): Int {
        var insertedCount = 0
        val titles = mutableSetOf<String>()
        movies.forEach { movie ->
            val normalizedMovie = movie.copy(title = normalizeMovieTitle(movie.title))
            if (movieTitleKey(normalizedMovie.title) !in titles &&
                insertMovieIfMissing(normalizedMovie)
            ) {
                titles += movieTitleKey(normalizedMovie.title)
                insertedCount++
            }
        }
        return insertedCount
    }

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Transaction
    suspend fun updateMovieIfUnique(movie: MovieEntity): Boolean {
        if (getMovieByTitleExcludingId(movie.title, movie.id) != null) {
            return false
        }
        updateMovie(movie.copy(title = normalizeMovieTitle(movie.title)))
        return true
    }

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("UPDATE movies SET inTrash = 1 WHERE id = :id")
    suspend fun moveToTrash(id: Int)

    @Query("UPDATE movies SET inTrash = 0 WHERE id = :id")
    suspend fun restoreMovie(id: Int)

    @Delete
    suspend fun permanentlyDeleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE inTrash = 1")
    suspend fun emptyTrash()

    @Query("UPDATE movies SET watched = :watched WHERE id = :id")
    suspend fun setWatched(id: Int, watched: Boolean)

    @Query("UPDATE movies SET collection = :collection WHERE id = :id")
    suspend fun moveMovie(id: Int, collection: String)

    @Query("""
        UPDATE movies
        SET episode = episode + 1
        WHERE id = :id AND isSeries = 1
    """)
    suspend fun nextEpisode(id: Int)

    @Query("""
        UPDATE movies
        SET episode = CASE
            WHEN episode > 1 THEN episode - 1
            ELSE 1
        END
        WHERE id = :id AND isSeries = 1
    """)
    suspend fun previousEpisode(id: Int)

    @Query("""
        UPDATE movies
        SET season = season + 1
        WHERE id = :id AND isSeries = 1
    """)
    suspend fun nextSeason(id: Int)

    @Query("""
        UPDATE movies
        SET season = CASE
            WHEN season > 1 THEN season - 1
            ELSE 1
        END
        WHERE id = :id AND isSeries = 1
    """)
    suspend fun previousSeason(id: Int)
}

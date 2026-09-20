package com.mahdi155000.clof_android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY id")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE collection = :collection ORDER BY id")
    fun getMoviesByCollection(collection: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id")
    fun observeMovie(id: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovie(id: Int): MovieEntity?

    @Insert
    suspend fun insertMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("UPDATE movies SET watched = :watched WHERE id = :id")
    suspend fun setWatched(id: Int, watched: Boolean)

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

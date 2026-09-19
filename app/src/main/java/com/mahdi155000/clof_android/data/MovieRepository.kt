package com.mahdi155000.clof_android.data

import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val movieDao: MovieDao
) {

    val allMovies: Flow<List<MovieEntity>> =
        movieDao.getAllMovies()

    fun observeMovie(id: Int): Flow<MovieEntity?> {
        return movieDao.observeMovie(id)
    }

    suspend fun getMovie(id: Int): MovieEntity? {
        return movieDao.getMovie(id)
    }

    suspend fun insertMovie(movie: MovieEntity) {
        movieDao.insertMovie(movie)
    }

    suspend fun updateMovie(movie: MovieEntity) {
        movieDao.updateMovie(movie)
    }

    suspend fun deleteMovie(movie: MovieEntity) {
        movieDao.deleteMovie(movie)
    }

    suspend fun setWatched(
        id: Int,
        watched: Boolean
    ) {
        movieDao.setWatched(
            id,
            watched
        )
    }

    suspend fun nextEpisode(id: Int) {
        movieDao.nextEpisode(id)
    }

    suspend fun previousEpisode(id: Int) {
        movieDao.previousEpisode(id)
    }
}
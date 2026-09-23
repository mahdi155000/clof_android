package com.mahdi155000.clof_android.data

import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val movieDao: MovieDao
) {

    val allMovies: Flow<List<MovieEntity>> =
        movieDao.getAllMovies()
    val trashMovies: Flow<List<MovieEntity>> =
        movieDao.getTrashMovies()

    fun observeMovie(id: Int): Flow<MovieEntity?> {
        return movieDao.observeMovie(id)
    }

    suspend fun getMovie(id: Int): MovieEntity? {
        return movieDao.getMovie(id)
    }

    suspend fun insertMovie(movie: MovieEntity): Boolean {
        return movieDao.insertMovieIfMissing(movie)
    }

    suspend fun insertMissingMovies(movies: List<MovieEntity>): Int {
        return movieDao.insertMissingMovies(movies)
    }

    suspend fun updateMovie(movie: MovieEntity): Boolean {
        return movieDao.updateMovieIfUnique(movie)
    }

    suspend fun deleteMovie(movie: MovieEntity) {
        movieDao.moveToTrash(movie.id)
    }

    suspend fun restoreMovie(movie: MovieEntity) {
        movieDao.restoreMovie(movie.id)
    }

    suspend fun permanentlyDeleteMovie(movie: MovieEntity) {
        movieDao.permanentlyDeleteMovie(movie)
    }

    suspend fun emptyTrash() {
        movieDao.emptyTrash()
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

    suspend fun moveMovie(id: Int, collection: String) {
        movieDao.moveMovie(id, collection)
    }

    suspend fun setPinned(id: Int, pinned: Boolean) {
        movieDao.setPinned(id, pinned)
    }

    suspend fun setCustomOrder(id: Int, customOrder: Int) {
        movieDao.setCustomOrder(id, customOrder)
    }

    suspend fun setPersonalRating(id: Int, rating: Int?) {
        movieDao.setPersonalRating(id, rating)
    }

    suspend fun setFavorite(id: Int, favorite: Boolean) {
        movieDao.setFavorite(id, favorite)
    }

    suspend fun nextEpisode(id: Int) {
        movieDao.nextEpisode(id)
    }

    suspend fun previousEpisode(id: Int) {
        movieDao.previousEpisode(id)
    }

    suspend fun nextSeason(id: Int) {
        movieDao.nextSeason(id)
    }

    suspend fun previousSeason(id: Int) {
        movieDao.previousSeason(id)
    }
}

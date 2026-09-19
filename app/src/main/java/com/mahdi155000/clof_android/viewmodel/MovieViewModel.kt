package com.mahdi155000.clof_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahdi155000.clof_android.data.AppDatabase
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.data.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        AppDatabase.getDatabase(application)

    private val repository =
        MovieRepository(
            database.movieDao()
        )

    val movies: StateFlow<List<MovieEntity>> =
        repository.allMovies.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun observeMovie(
        id: Int
    ): Flow<MovieEntity?> {
        return repository.observeMovie(id)
    }

    fun addMovie(
        title: String,
        genre: String = "",
        isSeries: Boolean = false,
        season: Int = 0,
        episode: Int = 0,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {

            repository.insertMovie(
                MovieEntity(
                    title = title,
                    genre = genre,
                    isSeries = isSeries,
                    season = season,
                    episode = episode
                )
            )

            onComplete()
        }
    }

    fun deleteMovie(
        movie: MovieEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {

            repository.deleteMovie(movie)

            onComplete()
        }
    }

    fun setWatched(
        movie: MovieEntity,
        watched: Boolean
    ) {
        viewModelScope.launch {

            repository.setWatched(
                movie.id,
                watched
            )
        }
    }

    fun nextEpisode(
        movie: MovieEntity
    ) {
        viewModelScope.launch {

            repository.nextEpisode(
                movie.id
            )
        }
    }

    fun previousEpisode(
        movie: MovieEntity
    ) {
        viewModelScope.launch {

            repository.previousEpisode(
                movie.id
            )
        }
    }

    fun updateMovie(
        movie: MovieEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {

            repository.updateMovie(movie)

            onComplete()
        }
    }
}
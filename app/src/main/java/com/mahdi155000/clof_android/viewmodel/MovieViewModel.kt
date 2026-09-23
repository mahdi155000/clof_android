package com.mahdi155000.clof_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahdi155000.clof_android.data.AppDatabase
import com.mahdi155000.clof_android.data.ClofDatabaseImporter
import com.mahdi155000.clof_android.data.ClofDatabaseExporter
import com.mahdi155000.clof_android.data.CollectionRepository
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.data.ImportResult
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.data.MovieRepository
import com.mahdi155000.clof_android.data.GenreRepository
import com.mahdi155000.clof_android.data.normalizeMovieTitle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val repository = MovieRepository(
        database.movieDao()
    )

    private val collectionRepository = CollectionRepository(database.collectionDao())
    private val genreRepository = GenreRepository(database.genreDao())

    init {
        viewModelScope.launch {
            collectionRepository.addMissingCollections(CollectionNames.DEFAULT)
            genreRepository.addMissingGenres(AppDatabase.defaultGenres)
        }
    }

    private val databaseImporter = ClofDatabaseImporter(
        application,
        repository,
        collectionRepository,
        genreRepository
    )
    private val databaseExporter = ClofDatabaseExporter(application, database)

    val collections: StateFlow<List<String>> =
        collectionRepository.collections.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CollectionNames.DEFAULT
        )

    val movies: StateFlow<List<MovieEntity>> =
        repository.allMovies.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val trashMovies: StateFlow<List<MovieEntity>> =
        repository.trashMovies.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val genres: StateFlow<List<String>> =
        genreRepository.genres.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppDatabase.defaultGenres
        )

    fun observeMovie(id: Int): Flow<MovieEntity?> {
        return repository.observeMovie(id)
    }

    fun addMovie(
        title: String,
        genre: String = "",
        isSeries: Boolean = false,
        season: Int = 0,
        episode: Int = 0,
        collection: String = CollectionNames.MAIN,
        onComplete: () -> Unit = {},
        onDuplicate: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val inserted = repository.insertMovie(
                    MovieEntity(
                        title = normalizeMovieTitle(title),
                        genre = genre,
                        isSeries = isSeries,
                        season = season,
                        episode = episode,
                        collection = collection
                    )
                )

                if (inserted) onComplete() else onDuplicate()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onError(exception)
            }
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

    fun restoreMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.restoreMovie(movie)
        }
    }

    fun permanentlyDeleteMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.permanentlyDeleteMovie(movie)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
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

    fun nextEpisode(movie: MovieEntity) {
        viewModelScope.launch {
            repository.nextEpisode(movie.id)
        }
    }

    fun previousEpisode(movie: MovieEntity) {
        viewModelScope.launch {
            repository.previousEpisode(movie.id)
        }
    }

    fun moveMovie(movie: MovieEntity, collection: String) {
        viewModelScope.launch {
            repository.moveMovie(movie.id, collection)
        }
    }

    fun nextSeason(movie: MovieEntity) {
        viewModelScope.launch {
            repository.nextSeason(movie.id)
        }
    }

    fun previousSeason(movie: MovieEntity) {
        viewModelScope.launch {
            repository.previousSeason(movie.id)
        }
    }

    fun importClofDatabase(
        uri: android.net.Uri,
        onComplete: (ImportResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                databaseImporter.importFrom(uri)
            }.onSuccess(onComplete)
                .onFailure(onError)
        }
    }

    fun exportClofDatabase(
        uri: android.net.Uri,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                databaseExporter.exportTo(uri)
            }.onSuccess { onComplete() }
                .onFailure(onError)
        }
    }

    fun addCollection(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            onComplete(collectionRepository.addCollection(name.trim()))
        }
    }

    fun renameCollection(
        oldName: String,
        newName: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onComplete(collectionRepository.renameCollection(oldName, newName.trim()))
        }
    }

    fun removeCollection(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            onComplete(collectionRepository.removeCollection(name))
        }
    }

    fun addGenre(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            onComplete(genreRepository.addGenre(name.trim()))
        }
    }

    fun renameGenre(
        oldName: String,
        newName: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onComplete(genreRepository.renameGenre(oldName, newName.trim()))
        }
    }

    fun removeGenre(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            onComplete(genreRepository.removeGenre(name))
        }
    }

    fun updateMovie(
        movie: MovieEntity,
        onComplete: () -> Unit = {},
        onDuplicate: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                if (repository.updateMovie(movie)) {
                    onComplete()
                } else {
                    onDuplicate()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onError(exception)
            }
        }
    }
}
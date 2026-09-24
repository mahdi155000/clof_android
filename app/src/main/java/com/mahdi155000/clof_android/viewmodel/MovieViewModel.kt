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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val repository = MovieRepository(
        database.movieDao()
    )

    private val collectionRepository = CollectionRepository(database.collectionDao())
    private val genreRepository = GenreRepository(database.genreDao())

    init {
        viewModelScope.launch(Dispatchers.IO) {
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
        notes: String? = null,
        personalRating: Int? = null,
        favorite: Boolean = false,
        onComplete: () -> Unit = {},
        onDuplicate: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inserted = repository.insertMovie(
                    MovieEntity(
                        title = normalizeMovieTitle(title),
                        genre = genre,
                        isSeries = isSeries,
                        season = season,
                        episode = episode,
                        collection = collection,
                        notes = notes?.trim()?.ifBlank { null },
                        personalRating = personalRating?.coerceIn(1, 5),
                        favorite = favorite
                    )
                )

                withContext(Dispatchers.Main) {
                    if (inserted) onComplete() else onDuplicate()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    onError(exception)
                }
            }
        }
    }

    fun deleteMovie(
        movie: MovieEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMovie(movie)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun restoreMovie(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreMovie(movie)
        }
    }

    fun permanentlyDeleteMovie(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteMovie(movie)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.emptyTrash()
        }
    }

    fun setWatched(
        movie: MovieEntity,
        watched: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setWatched(
                movie.id,
                watched
            )
        }
    }

    fun restoreMovieState(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMovie(movie)
        }
    }

    fun restoreGenre(name: String, movies: List<MovieEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            genreRepository.addGenre(name)
            movies.forEach { repository.updateMovie(it) }
        }
    }

    fun nextEpisode(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.nextEpisode(movie.id)
        }
    }

    fun previousEpisode(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.previousEpisode(movie.id)
        }
    }

    fun moveMovie(movie: MovieEntity, collection: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveMovie(movie.id, collection)
        }
    }

    fun setPinned(movie: MovieEntity, pinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPinned(movie.id, pinned)
        }
    }

    fun setCustomOrder(movie: MovieEntity, customOrder: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setCustomOrder(movie.id, customOrder)
        }
    }

    fun setPersonalRating(movie: MovieEntity, rating: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPersonalRating(movie.id, rating?.coerceIn(1, 5))
        }
    }

    fun setFavorite(movie: MovieEntity, favorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setFavorite(movie.id, favorite)
        }
    }

    fun nextSeason(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.nextSeason(movie.id)
        }
    }

    fun previousSeason(movie: MovieEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.previousSeason(movie.id)
        }
    }

    fun importClofDatabase(
        uri: android.net.Uri,
        onComplete: (ImportResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                databaseImporter.importFrom(uri)
            }.onSuccess { result ->
                withContext(Dispatchers.Main) { onComplete(result) }
            }.onFailure { error ->
                withContext(Dispatchers.Main) { onError(error) }
            }
        }
    }

    fun exportClofDatabase(
        uri: android.net.Uri,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                databaseExporter.exportTo(uri)
            }.onSuccess {
                withContext(Dispatchers.Main) { onComplete() }
            }.onFailure { error ->
                withContext(Dispatchers.Main) { onError(error) }
            }
        }
    }

    fun addCollection(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = collectionRepository.addCollection(name.trim())
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }

    fun renameCollection(
        oldName: String,
        newName: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = collectionRepository.renameCollection(oldName, newName.trim())
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }

    fun removeCollection(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = collectionRepository.removeCollection(name)
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }

    fun addGenre(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = genreRepository.addGenre(name.trim())
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }

    fun renameGenre(
        oldName: String,
        newName: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = genreRepository.renameGenre(oldName, newName.trim())
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }

    fun removeGenre(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = genreRepository.removeGenre(name)
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }

    fun updateMovie(
        movie: MovieEntity,
        onComplete: () -> Unit = {},
        onDuplicate: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updated = repository.updateMovie(movie)
                withContext(Dispatchers.Main) {
                    if (updated) onComplete() else onDuplicate()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    onError(exception)
                }
            }
        }
    }
}
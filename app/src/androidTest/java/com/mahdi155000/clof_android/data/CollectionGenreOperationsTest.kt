package com.mahdi155000.clof_android.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionGenreOperationsTest {
    private lateinit var database: AppDatabase
    private lateinit var movieDao: MovieDao
    private lateinit var collections: CollectionRepository
    private lateinit var genres: GenreRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        movieDao = database.movieDao()
        collections = CollectionRepository(database.collectionDao())
        genres = GenreRepository(database.genreDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun collectionOperations_renameAndRemoveMoveMoviesCorrectly() = runBlocking {
        assertTrue(collections.addCollection("Archive"))
        assertFalse(collections.addCollection(CollectionNames.MAIN))
        assertTrue(movieDao.insertMovieIfMissing(MovieEntity(title = "Archived", collection = "Archive")))
        val movie = movieDao.getMovieByTitle("Archived")!!

        assertTrue(collections.renameCollection("Archive", "Favorites"))
        assertEquals("Favorites", movieDao.getMovie(movie.id)?.collection)
        assertTrue(collections.collections.first().contains("Favorites"))
        assertFalse(collections.renameCollection("Favorites", CollectionNames.MAIN))

        assertTrue(collections.removeCollection("Favorites"))
        assertEquals(CollectionNames.MAIN, movieDao.getMovie(movie.id)?.collection)
        assertFalse(collections.removeCollection(CollectionNames.MAIN))
    }

    @Test
    fun genreOperations_renameAndRemoveUpdateMovieGenres() = runBlocking {
        assertTrue(genres.addGenre("Sci-Fi"))
        assertTrue(
            movieDao.insertMovieIfMissing(
                MovieEntity(title = "Genre movie", genre = "Drama, Sci-Fi")
            )
        )
        val movie = movieDao.getMovieByTitle("Genre movie")!!

        assertTrue(genres.renameGenre("Sci-Fi", "Science Fiction"))
        assertEquals("Drama, Science Fiction", movieDao.getMovie(movie.id)?.genre)
        assertTrue(genres.genres.first().contains("Science Fiction"))

        assertTrue(genres.removeGenre("Science Fiction"))
        assertEquals("Drama", movieDao.getMovie(movie.id)?.genre)
    }
}

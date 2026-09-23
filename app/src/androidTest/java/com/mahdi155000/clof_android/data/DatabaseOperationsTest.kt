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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseOperationsTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: MovieDao
    private lateinit var repository: MovieRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.movieDao()
        repository = MovieRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addMovie_normalizesTitleAndRejectsDuplicates() = runBlocking {
        assertTrue(repository.insertMovie(MovieEntity(title = "  Inception  ")))
        assertFalse(repository.insertMovie(MovieEntity(title = "inception")))

        val movie = dao.getMovieByTitle("INCEPTION")
        assertNotNull(movie)
        assertEquals("Inception", movie?.title)
    }

    @Test
    fun editMovie_updatesUniqueMovieAndRejectsDuplicateTitle() = runBlocking {
        assertTrue(repository.insertMovie(MovieEntity(title = "Before")))
        assertTrue(repository.insertMovie(MovieEntity(title = "Other")))
        val before = dao.getMovieByTitle("Before")!!
        val other = dao.getMovieByTitle("Other")!!

        assertTrue(repository.updateMovie(before.copy(title = "After", notes = "Updated")))
        assertFalse(repository.updateMovie(other.copy(title = "After")))

        assertEquals("Updated", dao.getMovie(before.id)?.notes)
        assertEquals("Other", dao.getMovie(other.id)?.title)
    }

    @Test
    fun moveWatchedAndSeriesProgression_updateMovieState() = runBlocking {
        val movie = MovieEntity(
            title = "Series",
            isSeries = true,
            season = 1,
            episode = 1
        )
        assertTrue(repository.insertMovie(movie))
        val stored = dao.getMovieByTitle(movie.title)!!

        repository.moveMovie(stored.id, "archive")
        repository.setWatched(stored.id, true)
        repository.nextEpisode(stored.id)
        repository.nextSeason(stored.id)

        val updated = dao.getMovie(stored.id)!!
        assertEquals("archive", updated.collection)
        assertTrue(updated.watched)
        assertEquals(2, updated.season)
        assertEquals(1, updated.episode)

        repository.previousSeason(updated.id)
        repository.previousEpisode(updated.id)
        val rewound = dao.getMovie(updated.id)!!
        assertEquals(1, rewound.season)
        assertEquals(1, rewound.episode)
    }

    @Test
    fun trashRestoreAndPermanentDelete_followExpectedLifecycle() = runBlocking {
        assertTrue(repository.insertMovie(MovieEntity(title = "Disposable")))
        val movie = dao.getMovieByTitle("Disposable")!!

        repository.deleteMovie(movie)
        assertTrue(dao.getTrashMovies().first().any { it.id == movie.id })
        assertTrue(dao.getAllMovies().first().none { it.id == movie.id })

        repository.restoreMovie(movie)
        assertTrue(dao.getAllMovies().first().any { it.id == movie.id })

        repository.deleteMovie(movie)
        repository.permanentlyDeleteMovie(movie)
        assertNull(dao.getMovie(movie.id))
    }
}

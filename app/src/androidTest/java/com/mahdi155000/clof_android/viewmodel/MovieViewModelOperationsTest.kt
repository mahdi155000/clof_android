package com.mahdi155000.clof_android.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mahdi155000.clof_android.data.AppDatabase
import com.mahdi155000.clof_android.data.MovieEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MovieViewModelOperationsTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val database = AppDatabase.getDatabase(application)

    @Test
    fun viewModel_supportsAddEditDuplicateCollectionProgressionAndTrash() = runBlocking {
        val viewModel = MovieViewModel(application)
        val title = "ViewModel ${System.currentTimeMillis()}"
        val added = CountDownLatch(1)
        viewModel.addMovie(title, onComplete = { added.countDown() })
        assertTrue(added.await(5, TimeUnit.SECONDS))

        val movie = database.movieDao().getMovieByTitle(title)!!
        val duplicate = CountDownLatch(1)
        viewModel.addMovie(title.lowercase(), onDuplicate = { duplicate.countDown() })
        assertTrue(duplicate.await(5, TimeUnit.SECONDS))

        val edited = CountDownLatch(1)
        viewModel.updateMovie(
            movie.copy(
                title = "$title edited",
                collection = "Archive",
                isSeries = true,
                season = 1,
                episode = 1
            ),
            onComplete = { edited.countDown() }
        )
        assertTrue(edited.await(5, TimeUnit.SECONDS))
        val updated = database.movieDao().getMovie(movie.id)!!
        assertEquals("Archive", updated.collection)

        viewModel.setWatched(updated, true)
        viewModel.nextEpisode(updated)
        eventually {
            val current = database.movieDao().getMovie(movie.id)
            current?.watched == true && current.episode == 2
        }

        viewModel.deleteMovie(updated)
        eventually {
            database.movieDao().getTrashMovies().first().any { it.id == movie.id }
        }
        viewModel.restoreMovie(updated)
        eventually {
            database.movieDao().getAllMovies().first().any { it.id == movie.id }
        }
        viewModel.deleteMovie(updated)
        viewModel.permanentlyDeleteMovie(updated)
        eventually {
            database.movieDao().getMovie(movie.id) == null
        }
    }

    private fun eventually(assertion: suspend () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        var satisfied = false
        while (System.nanoTime() < deadline && !satisfied) {
            satisfied = runBlocking { assertion() }
            if (!satisfied) Thread.sleep(50)
        }
        assertTrue(satisfied)
    }
}

package com.mahdi155000.clof_android

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mahdi155000.clof_android.data.AppDatabase
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeMovieFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addMovieScreen_addsMovieThroughTheRealViewModel() {
        val title = "Compose Add ${System.currentTimeMillis()}"
        var completed = false
        composeRule.activity.setContent {
            MaterialTheme {
                AddMovieScreen(viewModel()) { completed = true }
            }
        }

        composeRule.onNodeWithText("Title").performTextInput(title)
        composeRule.onNodeWithText("Add Movie").performClick()
        composeRule.waitUntil(5_000) { completed }

        val movie = runBlocking {
            AppDatabase.getDatabase(
                ApplicationProvider.getApplicationContext<Application>()
            ).movieDao().getMovieByTitle(title)
        }
        assertEquals(title, movie?.title)
    }

    @Test
    fun editMovieScreen_savesTitleChanges() {
        val database = AppDatabase.getDatabase(
            ApplicationProvider.getApplicationContext<Application>()
        )
        val originalTitle = "Compose Edit ${System.currentTimeMillis()}"
        runBlocking {
            database.movieDao().insertMovieIfMissing(MovieEntity(title = originalTitle))
        }
        val movie = runBlocking { database.movieDao().getMovieByTitle(originalTitle)!! }
        var completed = false

        composeRule.activity.setContent {
            MaterialTheme {
                EditMovieScreen(movie, viewModel()) { completed = true }
            }
        }

        val editedTitle = "$originalTitle Updated"
        composeRule.onNodeWithText("Title").performTextClearance()
        composeRule.onNodeWithText("Title").performTextInput(editedTitle)
        composeRule.onNodeWithText("Save Changes").performClick()
        composeRule.waitUntil(5_000) { completed }

        assertTrue(runBlocking {
            database.movieDao().getMovie(movie.id)?.title == editedTitle
        })
    }
}

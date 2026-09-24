package com.mahdi155000.clof_android

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasScrollToKeyAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mahdi155000.clof_android.data.AppDatabase
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LargeLibraryComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun movieList_rendersAndScrollsSmoothly_withLargeDataset() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val database = AppDatabase.getDatabase(application)

        // Seed 500 items into the database
        val largeList = List(500) { index ->
            MovieEntity(
                title = "Large Library Movie $index",
                genre = "Action",
                collection = "main"
            )
        }
        runBlocking {
            database.movieDao().insertMissingMovies(largeList)
        }

        composeRule.activity.setContent {
            MaterialTheme {
                val viewModel: MovieViewModel = viewModel()
                MovieList(
                    movies = largeList,
                    movieViewModel = viewModel,
                    onMovieClick = {},
                    onEdit = {},
                    onDelete = {},
                    onShowUndo = { _, _ -> }
                )
            }
        }

        // Verify initial item renders
        composeRule.onNodeWithText("Large Library Movie 0").assertExists()

        // Scroll to deep item
        composeRule.onNode(hasScrollToKeyAction()).performScrollToIndex(250)
        composeRule.waitForIdle()

        // Verify scrolled item exists
        composeRule.onNodeWithText("Large Library Movie 250").assertExists()
    }
}

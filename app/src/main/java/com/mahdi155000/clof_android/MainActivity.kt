package com.mahdi155000.clof_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)

        setContent {

            var darkMode by remember {
                mutableStateOf(settingsManager.isDarkMode())
            }

            MaterialTheme(
                colorScheme = if (darkMode) {
                    androidx.compose.material3.darkColorScheme()
                } else {
                    androidx.compose.material3.lightColorScheme()
                }
            ) {

                ClofApp(
                    darkMode = darkMode,
                    onDarkModeChange = { enabled ->
                        darkMode = enabled
                        settingsManager.setDarkMode(enabled)
                    }
                )
            }
        }
    }
}

@Composable
fun ClofApp(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    movieViewModel: MovieViewModel = viewModel()
) {
    var showAddMovieScreen by remember {
        mutableStateOf(false)
    }

    var movieBeingEdited by remember {
        mutableStateOf<MovieEntity?>(null)
    }

    if (movieBeingEdited != null) {

        val movie = movieBeingEdited!!

        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Button(
                    onClick = {
                        movieBeingEdited = null
                    },
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Back")
                }

                EditMovieScreen(
                    movie = movie,
                    movieViewModel = movieViewModel,
                    onMovieUpdated = {
                        movieBeingEdited = null
                    }
                )
            }
        }

    } else if (showAddMovieScreen) {

        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Button(
                    onClick = {
                        showAddMovieScreen = false
                    },
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Back")
                }

                AddMovieScreen(
                    movieViewModel = movieViewModel,
                    onMovieAdded = {
                        showAddMovieScreen = false
                    }
                )
            }
        }

    } else {

        val movies by movieViewModel.movies.collectAsState()

        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        showAddMovieScreen = true
                    }
                ) {
                    Text("+")
                }
            }
        ) { innerPadding ->

            MovieList(
                movies = movies,
                movieViewModel = movieViewModel,
                onEdit = { movie ->
                    movieBeingEdited = movie
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClofTopBar(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Text("CLOF")
        },
        actions = {

            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {

                Text("Dark")

                Switch(
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange
                )
            }
        }
    )
}


@Composable
fun MovieList(
    movies: List<MovieEntity>,
    movieViewModel: MovieViewModel,
    onEdit: (MovieEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "No movies yet",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Press + to add a movie."
            )
        }

    } else {

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            items(
                items = movies,
                key = { movie -> movie.id }
            ) { movie ->

                MovieItem(
                    movie = movie,
                    movieViewModel = movieViewModel,
                    onEdit = onEdit
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }
    }
}

@Composable
fun MovieItem(
    movie: MovieEntity,
    movieViewModel: MovieViewModel,
    onEdit: (MovieEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge
            )

            if (movie.genre.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = movie.genre
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (movie.isSeries) {

                Text(
                    text = "S${movie.season} E${movie.episode}"
                )
            }

            Text(
                text = if (movie.watched) {
                    "Watched"
                } else {
                    "Not watched"
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Watched / Unwatch
                Button(
                    onClick = {
                        movieViewModel.setWatched(
                            movie,
                            !movie.watched
                        )
                    }
                ) {
                    Text(
                        if (movie.watched) {
                            "Unwatch"
                        } else {
                            "Watched"
                        }
                    )
                }

                // Edit
                Button(
                    onClick = {
                        onEdit(movie)
                    }
                ) {
                    Text("Edit")
                }

                // Delete
                Button(
                    onClick = {
                        movieViewModel.deleteMovie(movie)
                    }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
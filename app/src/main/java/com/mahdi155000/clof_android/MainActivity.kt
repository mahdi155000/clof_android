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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

        setContent {
            MaterialTheme {
                ClofApp()
            }
        }
    }
}

@Composable
fun ClofApp(
    movieViewModel: MovieViewModel = viewModel()
) {
    val movies by movieViewModel.movies.collectAsState()

    Scaffold(
        topBar = {
            ClofTopBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    movieViewModel.addMovie(
                        title = "Test Movie",
                        genre = "Test"
                    )
                }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        MovieList(
            movies = movies,
            movieViewModel = movieViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClofTopBar() {
    TopAppBar(
        title = {
            Text("CLOF")
        }
    )
}

@Composable
fun MovieList(
    movies: List<MovieEntity>,
    movieViewModel: MovieViewModel,
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
                    movieViewModel = movieViewModel
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
    movieViewModel: MovieViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun MovieDetailsScreen(
    movieId: Int,
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onEdit: (MovieEntity) -> Unit
) {
    val movie by movieViewModel
        .observeMovie(movieId)
        .collectAsState(initial = null)

    if (movie == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Movie not found")

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onBack
            ) {
                Text("Back")
            }
        }

        return
    }

    val currentMovie = movie!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Button(
            onClick = onBack
        ) {
            Text("Back")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = currentMovie.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (currentMovie.genre.isNotBlank()) {
            Text(
                text = "Genre: ${currentMovie.genre}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        Text(
            text = if (currentMovie.isSeries) {
                "Type: Series"
            } else {
                "Type: Movie"
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (currentMovie.isSeries) {
            Text(
                text = "Season: ${currentMovie.season}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Episode: ${currentMovie.episode}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = { movieViewModel.previousEpisode(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Previous Episode")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { movieViewModel.nextEpisode(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next Episode")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { movieViewModel.previousSeason(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Previous Season")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { movieViewModel.nextSeason(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next Season")
            }
        } else {
            Text(
                text = if (currentMovie.watched) {
                    "Status: Watched"
                } else {
                    "Status: Not watched"
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    movieViewModel.setWatched(
                        currentMovie,
                        !currentMovie.watched
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (currentMovie.watched) {
                        "Mark as Unwatched"
                    } else {
                        "Mark as Watched"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentMovie.watched && currentMovie.collection != "watched") {
                Button(
                    onClick = {
                        movieViewModel.moveMovie(currentMovie, "watched")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Move to Watched collection")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Collection: ${currentMovie.collection}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = { onEdit(currentMovie) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Movie")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {
                movieViewModel.deleteMovie(
                    currentMovie,
                    onComplete = onBack
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Movie")
        }
    }
}
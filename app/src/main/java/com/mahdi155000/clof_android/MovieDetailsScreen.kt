package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel


@Composable
fun MovieDetailsScreen(
    movie: MovieEntity,
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // Back button
        Button(
            onClick = onBack
        ) {
            Text("Back")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // Title
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Genre
        if (movie.genre.isNotBlank()) {

            Text(
                text = "Genre: ${movie.genre}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        // Type
        Text(
            text = if (movie.isSeries) {
                "Type: Series"
            } else {
                "Type: Movie"
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // Series information
        if (movie.isSeries) {

            Text(
                text = "Season: ${movie.season}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Episode: ${movie.episode}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        // Watched status
        Text(
            text = if (movie.watched) {
                "Status: Watched"
            } else {
                "Status: Not watched"
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // Collection
        Text(
            text = "Collection: ${movie.collection}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // Watched button
        Button(
            onClick = {
                movieViewModel.setWatched(
                    movie,
                    !movie.watched
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (movie.watched) {
                    "Mark as Unwatched"
                } else {
                    "Mark as Watched"
                }
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Edit
        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Movie")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Delete
        Button(
            onClick = {
                movieViewModel.deleteMovie(movie)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Movie")
        }
    }
}
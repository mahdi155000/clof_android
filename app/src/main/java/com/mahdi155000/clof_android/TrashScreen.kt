package com.mahdi155000.clof_android

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun TrashScreen(
    movieViewModel: MovieViewModel,
    onBack: () -> Unit
) {
    val movies by movieViewModel.trashMovies.collectAsState()
    var showEmptyConfirmation by remember { mutableStateOf(false) }
    var movieToDelete by remember { mutableStateOf<MovieEntity?>(null) }

    if (showEmptyConfirmation) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmation = false },
            title = { Text("Empty trash?") },
            text = { Text("All movies and series in trash will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        movieViewModel.emptyTrash()
                        showEmptyConfirmation = false
                    }
                ) {
                    Text("Delete permanently")
                }
            },
            dismissButton = {
                Button(onClick = { showEmptyConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    movieToDelete?.let { movie ->
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            title = { Text("Delete permanently?") },
            text = { Text("\"${movie.title}\" cannot be restored after this.") },
            confirmButton = {
                Button(
                    onClick = {
                        movieViewModel.permanentlyDeleteMovie(movie)
                        movieToDelete = null
                    }
                ) {
                    Text("Delete permanently")
                }
            },
            dismissButton = {
                Button(onClick = { movieToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Trash")
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showEmptyConfirmation = true },
            enabled = movies.isNotEmpty()
        ) {
            Text("Clear trash")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (movies.isEmpty()) {
            Text("Trash is empty.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(movies, key = MovieEntity::id) { movie ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(movie.title)
                            Text(if (movie.isSeries) "Series" else "Movie")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { movieViewModel.restoreMovie(movie) }) {
                                Text("Restore")
                            }
                            Button(onClick = { movieToDelete = movie }) {
                                Text("Delete")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

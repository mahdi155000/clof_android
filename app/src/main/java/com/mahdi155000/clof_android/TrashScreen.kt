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
import androidx.compose.ui.res.stringResource
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
            title = { Text(stringResource(R.string.empty_trash_title)) },
            text = { Text(stringResource(R.string.empty_trash_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        movieViewModel.emptyTrash()
                        showEmptyConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.delete_permanently))
                }
            },
            dismissButton = {
                Button(onClick = { showEmptyConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    movieToDelete?.let { movie ->
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            title = { Text(stringResource(R.string.delete_permanently_title)) },
            text = { Text(stringResource(R.string.cannot_restore, movie.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        movieViewModel.permanentlyDeleteMovie(movie)
                        movieToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_permanently))
                }
            },
            dismissButton = {
                Button(onClick = { movieToDelete = null }) {
                    Text(stringResource(R.string.cancel))
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
            Text(stringResource(R.string.back))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.trash))
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showEmptyConfirmation = true },
            enabled = movies.isNotEmpty()
        ) {
            Text(stringResource(R.string.clear_trash))
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (movies.isEmpty()) {
            Text(stringResource(R.string.empty_trash))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(movies, key = MovieEntity::id) { movie ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(movie.title)
                            Text(stringResource(if (movie.isSeries) R.string.series else R.string.movie))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { movieViewModel.restoreMovie(movie) }) {
                                Text(stringResource(R.string.restore))
                            }
                            Button(onClick = { movieToDelete = movie }) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

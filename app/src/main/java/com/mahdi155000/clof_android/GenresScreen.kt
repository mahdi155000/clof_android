package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun GenresScreen(
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onShowUndo: (String, () -> Unit) -> Unit
) {
    val genreAddedMessage = stringResource(R.string.genre_added)
    val genreExistsMessage = stringResource(R.string.genre_exists)
    val genreRenamedMessage = stringResource(R.string.genre_renamed)
    val newGenreNameMessage = stringResource(R.string.new_genre_name)
    val genreRemovedMessage = stringResource(R.string.genre_removed)
    val genreRemoveErrorMessage = stringResource(R.string.genre_remove_error)
    val genres by movieViewModel.genres.collectAsState()
    var newGenreName by remember { mutableStateOf("") }
    var editingGenre by remember { mutableStateOf<String?>(null) }
    var editedName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var genreToRemove by remember { mutableStateOf<String?>(null) }
    val movies by movieViewModel.movies.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Button(onClick = onBack) {
            Text(stringResource(R.string.back))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.manage_genres))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newGenreName,
            onValueChange = { newGenreName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.new_genre)) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                movieViewModel.addGenre(newGenreName.trim()) { added ->
                    message = if (added) {
                        newGenreName = ""
                        genreAddedMessage
                    } else {
                        genreExistsMessage
                    }
                }
            },
            enabled = newGenreName.isNotBlank()
        ) {
            Text(stringResource(R.string.add_genre))
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(genres, key = { it }) { genre ->
                if (editingGenre == genre) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.genre_name)) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                movieViewModel.renameGenre(genre, editedName.trim()) { renamed ->
                                    message = if (renamed) {
                                        editingGenre = null
                                        genreRenamedMessage
                                    } else {
                                        newGenreNameMessage
                                    }
                                }
                            },
                            enabled = editedName.isNotBlank()
                        ) {
                            Text(stringResource(R.string.save))
                        }
                        Button(onClick = { editingGenre = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(genre, modifier = Modifier.padding(top = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    editingGenre = genre
                                    editedName = genre
                                }
                            ) {
                                Text(stringResource(R.string.rename))
                            }
                            Button(
                                onClick = { genreToRemove = genre }
                            ) {
                                Text(stringResource(R.string.remove))
                            }
                        }

                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        genreToRemove?.let { genre ->
            val affectedMovies = movies.filter { movie ->
                genre in movie.genre.split(",").map { it.trim() }
            }
            val removedGenreSnackbar = stringResource(R.string.genre_removed_snackbar, genre)
            AlertDialog(
                onDismissRequest = { genreToRemove = null },
                title = { Text(stringResource(R.string.remove_genre_title)) },
                text = {
                    Text(
                        if (affectedMovies.isEmpty()) {
                            stringResource(R.string.no_items_use_genre, genre)
                        } else {
                            val titles = affectedMovies.joinToString("\n") { "• ${it.title}" }
                            stringResource(
                                R.string.genre_removal_warning,
                                genre,
                                affectedMovies.size,
                                titles
                            )
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            genreToRemove = null
                            movieViewModel.removeGenre(genre) { removed ->
                                message = if (removed) {
                                    onShowUndo(removedGenreSnackbar) {
                                        movieViewModel.restoreGenre(genre, affectedMovies)
                                    }
                                    genreRemovedMessage
                                } else {
                                    genreRemoveErrorMessage
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                },
                dismissButton = {
                    Button(onClick = { genreToRemove = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

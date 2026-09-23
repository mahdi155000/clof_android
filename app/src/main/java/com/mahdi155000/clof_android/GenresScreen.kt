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
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun GenresScreen(
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onShowUndo: (String, () -> Unit) -> Unit
) {
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
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Manage genres")
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newGenreName,
            onValueChange = { newGenreName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New genre") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                movieViewModel.addGenre(newGenreName.trim()) { added ->
                    message = if (added) {
                        newGenreName = ""
                        "Genre added."
                    } else {
                        "That genre already exists."
                    }
                }
            },
            enabled = newGenreName.isNotBlank()
        ) {
            Text("Add genre")
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
                        label = { Text("Genre name") },
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
                                        "Genre renamed."
                                    } else {
                                        "Use a new genre name."
                                    }
                                }
                            },
                            enabled = editedName.isNotBlank()
                        ) {
                            Text("Save")
                        }
                        Button(onClick = { editingGenre = null }) {
                            Text("Cancel")
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
                                Text("Rename")
                            }
                            Button(
                                onClick = { genreToRemove = genre }
                            ) {
                                Text("Remove")
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
            AlertDialog(
                onDismissRequest = { genreToRemove = null },
                title = { Text("Remove genre?") },
                text = {
                    Text(
                        if (affectedMovies.isEmpty()) {
                            "No movies or series use \"$genre\"."
                        } else {
                            val titles = affectedMovies.joinToString("\n") { "• ${it.title}" }
                            "The genre \"$genre\" will be removed from " +
                                "${affectedMovies.size} movie(s)/series:\n\n$titles"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            genreToRemove = null
                            movieViewModel.removeGenre(genre) { removed ->
                                message = if (removed) {
                                    onShowUndo("Removed genre \"$genre\"") {
                                        movieViewModel.restoreGenre(genre, affectedMovies)
                                    }
                                    "Genre removed from the list and its movies."
                                } else {
                                    "Genre could not be removed."
                                }
                            }
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    Button(onClick = { genreToRemove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

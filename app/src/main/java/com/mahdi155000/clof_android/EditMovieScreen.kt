package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.data.withEdits
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun EditMovieScreen(
    movie: MovieEntity,
    movieViewModel: MovieViewModel,
    onMovieUpdated: () -> Unit
) {
    var title by remember { mutableStateOf(movie.title) }
    var genresForMovie by remember {
        mutableStateOf(movie.genre.split(",").map { it.trim() }.filter { it.isNotBlank() })
    }
    var collection by remember { mutableStateOf(movie.collection) }
    var isSeries by remember { mutableStateOf(movie.isSeries) }
    var season by remember { mutableStateOf(movie.season.toString()) }
    var episode by remember { mutableStateOf(movie.episode.toString()) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val collections by movieViewModel.collections.collectAsState()
    val genres by movieViewModel.genres.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Edit Movie")

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(12.dp))

        GenrePicker(
            selectedGenres = genresForMovie,
            genres = (genres + genresForMovie).filter { it.isNotBlank() }.distinct(),
            onGenresChanged = { genresForMovie = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        CollectionPicker(
            selectedCollection = collection,
            collections = (collections + collection).distinct(),
            onCollectionSelected = { collection = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Series")

            Switch(
                checked = isSeries,
                onCheckedChange = { isSeries = it },
                enabled = !isSaving
            )
        }

        if (isSeries) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = season,
                onValueChange = { season = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Season") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = episode,
                onValueChange = { episode = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Episode") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                enabled = !isSaving
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        saveError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (title.isBlank() || isSaving) {
                    return@Button
                }

                isSaving = true
                saveError = null

                val updatedMovie = movie.withEdits(
                    title = title,
                    genre = genresForMovie.joinToString(", "),
                    collection = collection,
                    isSeries = isSeries,
                    season = season,
                    episode = episode
                )

                movieViewModel.updateMovie(
                    movie = updatedMovie,
                    onComplete = {
                        isSaving = false
                        onMovieUpdated()
                    },
                    onError = {
                        isSaving = false
                        saveError = "Couldn't update the movie. Please try again."
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && !isSaving
        ) {
            Text(
                if (isSaving) {
                    "Saving..."
                } else {
                    "Save Changes"
                }
            )
        }
    }
}

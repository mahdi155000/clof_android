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
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.data.toPositiveIntOrNull
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun AddMovieScreen(
    movieViewModel: MovieViewModel,
    onMovieAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var genresForMovie by remember { mutableStateOf<List<String>>(emptyList()) }
    var collection by remember { mutableStateOf(CollectionNames.MAIN) }
    var isSeries by remember { mutableStateOf(false) }
    var season by remember { mutableStateOf("1") }
    var episode by remember { mutableStateOf("1") }
    var seasonError by remember { mutableStateOf<String?>(null) }
    var episodeError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val collections by movieViewModel.collections.collectAsState()
    val genres by movieViewModel.genres.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Add Movie")

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
            genres = genres,
            onGenresChanged = { genresForMovie = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        CollectionPicker(
            selectedCollection = collection,
            collections = collections,
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
                onValueChange = {
                    season = it
                    seasonError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Season") },
                isError = seasonError != null,
                supportingText = seasonError?.let { message -> { Text(message) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = episode,
                onValueChange = {
                    episode = it
                    episodeError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Episode") },
                isError = episodeError != null,
                supportingText = episodeError?.let { message -> { Text(message) } },
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
                if (isSaving) {
                    return@Button
                }

                val parsedSeason = if (isSeries) season.toPositiveIntOrNull() else 0
                val parsedEpisode = if (isSeries) episode.toPositiveIntOrNull() else 0
                seasonError = if (isSeries && parsedSeason == null) {
                    "Season must be a positive integer."
                } else {
                    null
                }
                episodeError = if (isSeries && parsedEpisode == null) {
                    "Episode must be a positive integer."
                } else {
                    null
                }

                if (title.isBlank() || (isSeries && (parsedSeason == null || parsedEpisode == null))) {
                    return@Button
                }

                isSaving = true
                saveError = null

                movieViewModel.addMovie(
                    title = title.trim(),
                    genre = genresForMovie.joinToString(", "),
                    collection = collection.trim().ifBlank { CollectionNames.MAIN },
                    isSeries = isSeries,
                    season = parsedSeason ?: 0,
                    episode = parsedEpisode ?: 0,
                    onComplete = {
                        isSaving = false
                        onMovieAdded()
                    },
                    onDuplicate = {
                        isSaving = false
                        saveError = "A movie with this title already exists."
                    },
                    onError = {
                        isSaving = false
                        saveError = "Couldn't add the movie. Please try again."
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
                    "Add Movie"
                }
            )
        }
    }
}

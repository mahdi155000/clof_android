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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.data.toPositiveIntOrNull
import com.mahdi155000.clof_android.data.withEdits
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun EditMovieScreen(
    movie: MovieEntity,
    movieViewModel: MovieViewModel,
    onMovieUpdated: () -> Unit
) {
    var title by rememberSaveable(movie.id) { mutableStateOf(movie.title) }
    var genresForMovie by rememberSaveable(movie.id) {
        mutableStateOf(movie.genre.split(",").map { it.trim() }.filter { it.isNotBlank() })
    }
    var collection by rememberSaveable(movie.id) { mutableStateOf(movie.collection) }
    var isSeries by rememberSaveable(movie.id) { mutableStateOf(movie.isSeries) }
    var season by rememberSaveable(movie.id) { mutableStateOf(movie.season.toString()) }
    var episode by rememberSaveable(movie.id) { mutableStateOf(movie.episode.toString()) }
    var notes by rememberSaveable(movie.id) { mutableStateOf(movie.notes.orEmpty()) }
    var rating by rememberSaveable(movie.id) {
        mutableStateOf(movie.personalRating?.toString().orEmpty())
    }
    var favorite by rememberSaveable(movie.id) { mutableStateOf(movie.favorite) }
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") },
            minLines = 3,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = rating,
            onValueChange = { value ->
                if (value.isEmpty() || value.toIntOrNull()?.let { it in 1..5 } == true) {
                    rating = value
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Personal rating (1-5, optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = !isSaving
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Favorite")
            Switch(
                checked = favorite,
                onCheckedChange = { favorite = it },
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

                val updatedMovie = movie.withEdits(
                    title = title,
                    genre = genresForMovie.joinToString(", "),
                    collection = collection,
                    isSeries = isSeries,
                    season = season,
                    episode = episode,
                    notes = notes
                    , personalRating = rating.toIntOrNull()
                    , favorite = favorite
                )

                movieViewModel.updateMovie(
                    movie = updatedMovie,
                    onComplete = {
                        isSaving = false
                        onMovieUpdated()
                    },
                    onDuplicate = {
                        isSaving = false
                        saveError = "A movie with this title already exists."
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

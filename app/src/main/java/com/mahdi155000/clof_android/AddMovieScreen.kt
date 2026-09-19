package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun AddMovieScreen(
    movieViewModel: MovieViewModel,
    onMovieAdded: () -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var genre by remember {
        mutableStateOf("")
    }

    var isSeries by remember {
        mutableStateOf(false)
    }

    var season by remember {
        mutableStateOf("1")
    }

    var episode by remember {
        mutableStateOf("1")
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Top
    ) {

        Text("Add Movie")

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("Title")
            },
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = genre,
            onValueChange = {
                genre = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text("Genre")
            },
            singleLine = true,
            enabled = !isSaving
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text("Series")

            Switch(
                checked = isSeries,
                onCheckedChange = {
                    isSeries = it
                },
                enabled = !isSaving
            )
        }

        if (isSeries) {

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = season,
                onValueChange = {
                    season = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Season")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),
                singleLine = true,
                enabled = !isSaving
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            OutlinedTextField(
                value = episode,
                onValueChange = {
                    episode = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text("Episode")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),
                singleLine = true,
                enabled = !isSaving
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Button(
            onClick = {

                if (
                    title.isBlank() ||
                    isSaving
                ) {
                    return@Button
                }

                isSaving = true

                movieViewModel.addMovie(
                    title = title.trim(),
                    genre = genre.trim(),
                    isSeries = isSeries,
                    season =
                        if (isSeries) {
                            season.toIntOrNull()
                                ?: 1
                        } else {
                            0
                        },
                    episode =
                        if (isSeries) {
                            episode.toIntOrNull()
                                ?: 1
                        } else {
                            0
                        },
                    onComplete = {
                        onMovieAdded()
                    }
                )
            },
            modifier =
                Modifier.fillMaxWidth(),
            enabled =
                title.isNotBlank() &&
                        !isSaving
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
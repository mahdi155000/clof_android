package com.mahdi155000.clof_android

import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun GenrePicker(
    selectedGenres: List<String>,
    genres: List<String>,
    onGenresChanged: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Button(onClick = { expanded = true }) {
        Text(
            if (selectedGenres.isEmpty()) {
                "Genres: None"
            } else {
                "Genres: ${selectedGenres.joinToString(", ")}"
            }
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("None") },
            onClick = {
                onGenresChanged(emptyList())
                expanded = false
            }
        )
        genres.forEach { genre ->
            DropdownMenuItem(
                text = {
                    Text(if (genre in selectedGenres) "✓ $genre" else genre)
                },
                onClick = {
                    onGenresChanged(
                        if (genre in selectedGenres) {
                            selectedGenres - genre
                        } else {
                            selectedGenres + genre
                        }
                    )
                }
            )
        }
    }
}

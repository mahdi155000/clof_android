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
    selectedGenre: String,
    genres: List<String>,
    onGenreSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Button(onClick = { expanded = true }) {
        Text(if (selectedGenre.isBlank()) "Genre: None" else "Genre: $selectedGenre")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("None") },
            onClick = {
                onGenreSelected("")
                expanded = false
            }
        )
        genres.forEach { genre ->
            DropdownMenuItem(
                text = { Text(genre) },
                onClick = {
                    onGenreSelected(genre)
                    expanded = false
                }
            )
        }
    }
}

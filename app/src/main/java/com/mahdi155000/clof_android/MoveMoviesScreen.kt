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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
fun MoveMoviesScreen(
    movieViewModel: MovieViewModel,
    onBack: () -> Unit
) {
    val movies by movieViewModel.movies.collectAsState()
    val collections by movieViewModel.collections.collectAsState()
    var expandedMovieId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Move movies and series")
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(movies, key = MovieEntity::id) { movie ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(movie.title)
                        Text("Collection: ${movie.collection}")
                    }

                    Column {
                        Button(onClick = { expandedMovieId = movie.id }) {
                            Text("Move")
                        }
                        DropdownMenu(
                            expanded = expandedMovieId == movie.id,
                            onDismissRequest = { expandedMovieId = null }
                        ) {
                            collections
                                .filter { it != movie.collection }
                                .forEach { collection ->
                                    DropdownMenuItem(
                                        text = { Text(collection) },
                                        onClick = {
                                            movieViewModel.moveMovie(movie, collection)
                                            expandedMovieId = null
                                        }
                                    )
                                }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

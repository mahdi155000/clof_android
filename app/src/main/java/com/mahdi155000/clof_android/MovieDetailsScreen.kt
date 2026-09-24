package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun MovieDetailsScreen(
    movieId: Int,
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onEdit: (MovieEntity) -> Unit,
    onShowUndo: (String, () -> Unit) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val movie by movieViewModel
        .observeMovie(movieId)
        .collectAsState(initial = null)

    if (movie == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.movie_not_found))

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onBack
            ) {
                Text(stringResource(R.string.back))
            }
        }

        return
    }

    val currentMovie = movie!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Button(
            onClick = onBack
        ) {
            Text(stringResource(R.string.back))
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = currentMovie.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (currentMovie.genre.isNotBlank()) {
            Text(
                text = stringResource(R.string.genre_value, currentMovie.genre),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        if (!currentMovie.notes.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.notes_value, currentMovie.notes),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = if (currentMovie.isSeries) {
                stringResource(R.string.type_value, stringResource(R.string.series))
            } else {
                stringResource(R.string.type_value, stringResource(R.string.movie))
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = stringResource(
                R.string.rating_value,
                currentMovie.personalRating?.let { "$it/5" }
                    ?: stringResource(R.string.not_rated)
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                movieViewModel.setFavorite(currentMovie, !currentMovie.favorite)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (currentMovie.favorite) R.string.remove_favorite else R.string.mark_favorite
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                movieViewModel.setPersonalRating(
                    currentMovie,
                    if (currentMovie.personalRating == null) 5 else null
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (currentMovie.personalRating == null) {
                    stringResource(R.string.rate_five)
                } else {
                    stringResource(R.string.clear_rating)
                }
            )
        }

        if (currentMovie.isSeries) {
            Text(
                text = if (currentMovie.watched) {
                    stringResource(R.string.status_value, stringResource(R.string.completed))
                } else {
                    stringResource(R.string.status_value, stringResource(R.string.in_progress))
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.season_value, currentMovie.season),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = stringResource(R.string.episode_value, currentMovie.episode),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = { movieViewModel.previousEpisode(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.previous_episode))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { movieViewModel.nextEpisode(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.next_episode))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { movieViewModel.previousSeason(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.previous_season))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { movieViewModel.nextSeason(currentMovie) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.next_season_episode_one))
            }

            Spacer(modifier = Modifier.height(12.dp))

            val markedSeriesMessage = stringResource(
                R.string.series_marked,
                stringResource(
                    if (currentMovie.watched) R.string.in_progress else R.string.completed
                )
            )
            Button(
                onClick = {
                    val previous = currentMovie.watched
                    movieViewModel.setWatched(currentMovie, !previous)
                    onShowUndo(
                        markedSeriesMessage
                    ) {
                        movieViewModel.setWatched(currentMovie, previous)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (currentMovie.watched) {
                        stringResource(R.string.mark_series_in_progress)
                    } else {
                        stringResource(R.string.mark_series_completed)
                    }
                )
            }
        } else {
            Text(
                text = if (currentMovie.watched) {
                    stringResource(R.string.status_value, stringResource(R.string.watched))
                } else {
                    stringResource(R.string.status_value, stringResource(R.string.not_watched))
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            val markedStatusMessage = stringResource(
                R.string.marked_status,
                stringResource(if (currentMovie.watched) R.string.unwatched else R.string.watched)
            )
            Button(
                onClick = {
                    val previous = currentMovie.watched
                    movieViewModel.setWatched(
                        currentMovie,
                        !previous
                    )
                    onShowUndo(markedStatusMessage) {
                        movieViewModel.setWatched(currentMovie, previous)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (currentMovie.watched) {
                        stringResource(R.string.mark_as_unwatched)
                    } else {
                        stringResource(R.string.mark_as_watched)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentMovie.watched &&
                currentMovie.collection != CollectionNames.WATCHED
            ) {
                Button(
                    onClick = {
                        movieViewModel.moveMovie(currentMovie, CollectionNames.WATCHED)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.move_to_watched_collection))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.collection_label, currentMovie.collection),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = { onEdit(currentMovie) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.edit_movie))
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.move_to_trash))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.move_to_trash_title)) },
            text = { Text(stringResource(R.string.restorable_from_trash, currentMovie.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        movieViewModel.deleteMovie(currentMovie, onBack)
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.move_to_trash))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
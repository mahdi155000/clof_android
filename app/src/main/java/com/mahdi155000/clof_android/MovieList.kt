package com.mahdi155000.clof_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MovieList(
    movies: List<MovieEntity>,
    movieViewModel: MovieViewModel,
    onMovieClick: (MovieEntity) -> Unit,
    onEdit: (MovieEntity) -> Unit,
    onDelete: (MovieEntity) -> Unit,
    selectedCollection: String? = null,
    collectionNames: List<String> = emptyList(),
    onShowUndo: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember {
        mutableStateOf("")
    }

    var typeFilter by remember {
        mutableStateOf("All")
    }

    var watchedFilter by remember {
        mutableStateOf("All")
    }
    var favoriteFilter by remember { mutableStateOf("All") }
    var ratingFilter by remember { mutableStateOf("All") }

    var collectionFilter by remember {
        mutableStateOf("All")
    }

    var sortOption by remember {
        mutableStateOf("Recently Added")
    }
    var reorderMode by remember { mutableStateOf(false) }

    var showFilters by remember {
        mutableStateOf(false)
    }

    val collections = remember(collectionNames) {
        collectionNames
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    LaunchedEffect(selectedCollection) {
        collectionFilter = "All"
    }

    LaunchedEffect(collections) {
        if (collectionFilter != "All" && collectionFilter !in collections) {
            collectionFilter = "All"
        }
    }

    val filteredMovies by produceState(
        initialValue = emptyList<MovieEntity>(),
        movies,
        searchText,
        typeFilter,
        watchedFilter,
        favoriteFilter,
        ratingFilter,
        selectedCollection,
        collectionFilter,
        sortOption
    ) {
        value = withContext(Dispatchers.Default) {
            filterAndSortMovies(
                movies = movies,
                searchText = searchText,
                typeFilter = typeFilter,
                watchedFilter = watchedFilter,
                favoriteFilter = favoriteFilter,
                ratingFilter = ratingFilter,
                selectedCollection = selectedCollection,
                collectionFilter = collectionFilter,
                sortOption = sortOption
            )
        }
    }

    val onToggleReorderMode = remember { { reorderMode = true } }
    val onMoveItem = remember(filteredMovies, movieViewModel) {
        { movie: MovieEntity, direction: Int ->
            val index = filteredMovies.indexOfFirst { it.id == movie.id }
            val targetIndex = index + direction
            if (index >= 0 && targetIndex in filteredMovies.indices) {
                val target = filteredMovies[targetIndex]
                movieViewModel.setCustomOrder(movie, target.customOrder)
                movieViewModel.setCustomOrder(target, movie.customOrder)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            label = {
                Text(stringResource(R.string.search))
            },
            placeholder = {
                Text(stringResource(R.string.search_movies_series))
            },
            singleLine = true
        )

        Button(
            onClick = {
                showFilters = !showFilters
            },
            modifier = Modifier.padding(
                horizontal = 12.dp
            )
        ) {
            Text(
                if (showFilters) {
                    stringResource(R.string.hide_filters)
                } else {
                    stringResource(R.string.show_filters)
                }
            )
        }

        Button(
            onClick = { reorderMode = !reorderMode },
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                stringResource(if (reorderMode) R.string.done_selecting else R.string.select_items)
            )
        }

        if (showFilters) {
            Text(
                text = stringResource(R.string.type),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 12.dp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = stringResource(R.string.all),
                    selected = typeFilter == "All"
                ) { typeFilter = "All" }
                FilterButton(
                    text = stringResource(R.string.movies),
                    selected = typeFilter == "Movies"
                ) { typeFilter = "Movies" }
                FilterButton(
                    text = stringResource(R.string.series),
                    selected = typeFilter == "Series"
                ) { typeFilter = "Series" }
            }

            Text(
                text = stringResource(R.string.watched),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 12.dp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = stringResource(R.string.all),
                    selected = watchedFilter == "All"
                ) { watchedFilter = "All" }
                FilterButton(
                    text = stringResource(R.string.watched),
                    selected = watchedFilter == "Watched"
                ) { watchedFilter = "Watched" }
                FilterButton(
                    text = stringResource(R.string.unwatched),
                    selected = watchedFilter == "Unwatched"
                ) { watchedFilter = "Unwatched" }
            }

            Text(
                text = stringResource(R.string.favorites),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp, top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(stringResource(R.string.all), favoriteFilter == "All") {
                    favoriteFilter = "All"
                }
                FilterButton(
                    stringResource(R.string.favorites),
                    favoriteFilter == "Favorites"
                ) { favoriteFilter = "Favorites" }
            }

            Text(
                text = stringResource(R.string.rating),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp, top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(stringResource(R.string.all), ratingFilter == "All") {
                    ratingFilter = "All"
                }
                FilterButton(stringResource(R.string.rated), ratingFilter == "Rated") {
                    ratingFilter = "Rated"
                }
                FilterButton(stringResource(R.string.unrated), ratingFilter == "Unrated") {
                    ratingFilter = "Unrated"
                }
            }

            Text(
                text = stringResource(R.string.collection),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 12.dp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = stringResource(R.string.custom),
                    selected = sortOption == "Custom Order"
                ) {
                    sortOption = if (sortOption == "Custom Order") {
                        "Recently Added"
                    } else {
                        "Custom Order"
                    }
                }
                FilterButton(
                    text = stringResource(R.string.all),
                    selected = collectionFilter == "All"
                ) {
                    collectionFilter = "All"
                }
                collections.forEach { collection ->
                    FilterButton(
                        text = collection,
                        selected = collectionFilter == collection
                    ) {
                        collectionFilter = collection
                    }
                }
            }

            Text(
                text = stringResource(R.string.sort),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 12.dp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(
                    text = stringResource(R.string.recent),
                    selected = sortOption == "Recently Added"
                ) { sortOption = "Recently Added" }
                FilterButton(
                    text = stringResource(R.string.oldest),
                    selected = sortOption == "Oldest Added"
                ) { sortOption = "Oldest Added" }
                FilterButton(
                    text = stringResource(R.string.a_z),
                    selected = sortOption == "Title A-Z"
                ) { sortOption = "Title A-Z" }
                FilterButton(
                    text = stringResource(R.string.z_a),
                    selected = sortOption == "Title Z-A"
                ) { sortOption = "Title Z-A" }
                FilterButton(
                    text = stringResource(R.string.collection),
                    selected = sortOption == "Collection A-Z"
                ) { sortOption = "Collection A-Z" }
                FilterButton(
                    text = stringResource(R.string.genre),
                    selected = sortOption == "Genre A-Z"
                ) { sortOption = "Genre A-Z" }
                FilterButton(
                    text = stringResource(R.string.watched),
                    selected = sortOption == "Watched First"
                ) { sortOption = "Watched First" }
                FilterButton(
                    text = stringResource(R.string.progress),
                    selected = sortOption == "Series Progress"
                ) { sortOption = "Series Progress" }
                FilterButton(
                    text = stringResource(R.string.favorites),
                    selected = sortOption == "Favorites First"
                ) { sortOption = "Favorites First" }
                FilterButton(
                    text = stringResource(R.string.rating),
                    selected = sortOption == "Highest Rated"
                ) { sortOption = "Highest Rated" }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        if (filteredMovies.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (movies.isEmpty()) {
                        stringResource(R.string.no_movies_yet)
                    } else {
                        stringResource(R.string.no_results_found)
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = if (movies.isEmpty()) {
                        stringResource(R.string.press_plus_to_add)
                    } else {
                        stringResource(R.string.change_search_filters)
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                )
            ) {
                items(
                    items = filteredMovies,
                    key = { movie ->
                        movie.id
                    }
                ) { movie ->
                    MovieItem(
                        movie = movie,
                        movieViewModel = movieViewModel,
                        onMovieClick = onMovieClick,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onShowUndo = onShowUndo,
                        reorderMode = reorderMode,
                        onToggleReorderMode = onToggleReorderMode,
                        onMove = onMoveItem
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }
        }
    }
}

fun filterAndSortMovies(
    movies: List<MovieEntity>,
    searchText: String,
    typeFilter: String,
    watchedFilter: String,
    favoriteFilter: String,
    ratingFilter: String,
    selectedCollection: String?,
    collectionFilter: String,
    sortOption: String
): List<MovieEntity> {
    if (movies.isEmpty()) return emptyList()

    val trimmedSearch = searchText.trim()
    val hasSearch = trimmedSearch.isNotEmpty()

    val filtered = movies.filter { movie ->
        if (hasSearch) {
            val matchesSearch = movie.title.contains(trimmedSearch, ignoreCase = true) ||
                movie.genre.contains(trimmedSearch, ignoreCase = true) ||
                movie.collection.contains(trimmedSearch, ignoreCase = true) ||
                (movie.isSeries && (
                    "season ${movie.season}".contains(trimmedSearch, ignoreCase = true) ||
                    "episode ${movie.episode}".contains(trimmedSearch, ignoreCase = true) ||
                    movie.season.toString().contains(trimmedSearch, ignoreCase = true) ||
                    movie.episode.toString().contains(trimmedSearch, ignoreCase = true)
                ))
            if (!matchesSearch) return@filter false
        }

        when (typeFilter) {
            "Movies" -> if (movie.isSeries) return@filter false
            "Series" -> if (!movie.isSeries) return@filter false
        }

        when (watchedFilter) {
            "Watched" -> if (!movie.watched) return@filter false
            "Unwatched" -> if (movie.watched) return@filter false
        }

        if (favoriteFilter == "Favorites" && !movie.favorite) return@filter false

        when (ratingFilter) {
            "Rated" -> if (movie.personalRating == null) return@filter false
            "Unrated" -> if (movie.personalRating != null) return@filter false
        }

        if (selectedCollection != null && movie.collection != selectedCollection) return@filter false
        if (collectionFilter != "All" && movie.collection != collectionFilter) return@filter false

        true
    }

    if (filtered.isEmpty()) return emptyList()

    val ordered = when (sortOption) {
        "Custom Order" -> filtered.sortedBy { it.customOrder }
        "Title A-Z" -> filtered.sortedBy { it.title.lowercase() }
        "Title Z-A" -> filtered.sortedByDescending { it.title.lowercase() }
        "Oldest Added" -> filtered.sortedBy { it.createdAt }
        "Collection A-Z" -> filtered.sortedWith(
            compareBy<MovieEntity> { it.collection.lowercase() }
                .thenBy { it.title.lowercase() }
        )
        "Genre A-Z" -> filtered.sortedWith(
            compareBy<MovieEntity> { it.genre.lowercase() }
                .thenBy { it.title.lowercase() }
        )
        "Watched First" -> filtered.sortedWith(
            compareByDescending<MovieEntity> { it.watched }
                .thenBy { it.title.lowercase() }
        )
        "Favorites First" -> filtered.sortedWith(
            compareByDescending<MovieEntity> { it.favorite }
                .thenBy { it.title.lowercase() }
        )
        "Highest Rated" -> filtered.sortedWith(
            compareByDescending<MovieEntity> { it.personalRating ?: 0 }
                .thenBy { it.title.lowercase() }
        )
        "Series Progress" -> filtered.sortedWith(
            compareBy<MovieEntity> { !it.isSeries }
                .thenBy { if (it.isSeries) it.season else 0 }
                .thenBy { if (it.isSeries) it.episode else 0 }
                .thenBy { it.title.lowercase() }
        )
        else -> filtered.sortedByDescending { it.createdAt }
    }

    val anyPinned = ordered.any { it.pinned }
    if (!anyPinned) return ordered

    val sortRanks = ordered.indices.associateBy { ordered[it].id }
    return ordered.sortedWith(
        compareByDescending<MovieEntity> { it.pinned }
            .thenBy { sortRanks[it.id] ?: Int.MAX_VALUE }
    )
}

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick
    ) {
        Text(
            if (selected) {
                stringResource(R.string.selected_item, text)
            } else {
                text
            }
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun MovieItem(
    movie: MovieEntity,
    movieViewModel: MovieViewModel,
    onMovieClick: (MovieEntity) -> Unit,
    onEdit: (MovieEntity) -> Unit,
    onDelete: (MovieEntity) -> Unit,
    onShowUndo: (String, () -> Unit) -> Unit,
    reorderMode: Boolean,
    onToggleReorderMode: () -> Unit,
    onMove: (MovieEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val resources = LocalContext.current.resources
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onMovieClick(movie) },
                onLongClick = onToggleReorderMode
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge
            )
            if (reorderMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { movieViewModel.setPinned(movie, !movie.pinned) }) {
                        Text(stringResource(if (movie.pinned) R.string.unpin else R.string.pin))
                    }
                    Button(onClick = { onMove(movie, -1) }) {
                        Text(stringResource(R.string.move_up))
                    }
                    Button(onClick = { onMove(movie, 1) }) {
                        Text(stringResource(R.string.move_down))
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            if (movie.genre.isNotBlank()) {
                Text(
                    text = movie.genre,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )
            }

            Text(
                text = if (movie.isSeries) {
                    stringResource(R.string.series)
                } else {
                    stringResource(R.string.movie)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (movie.isSeries) {
                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = stringResource(R.string.season_episode, movie.season, movie.episode),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (!movie.isSeries) {
                Text(
                    text = if (movie.watched) {
                        stringResource(R.string.watched)
                    } else {
                        stringResource(R.string.not_watched)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = if (movie.watched) {
                        stringResource(R.string.completed)
                    } else {
                        stringResource(R.string.in_progress)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (movie.isSeries) {
                            movieViewModel.nextEpisode(movie)
                        } else {
                            val previous = movie.watched
                            movieViewModel.setWatched(
                                movie,
                                !previous
                            )
                            val markedStatusMessage = resources.getString(
                                R.string.marked_status,
                                resources.getString(if (movie.watched) R.string.unwatched else R.string.watched)
                            )
                            onShowUndo(markedStatusMessage) {
                                movieViewModel.setWatched(movie, previous)
                            }
                        }
                    }
                ) {
                    Text(
                        if (movie.isSeries) {
                            stringResource(R.string.next_episode)
                        } else if (movie.watched) {
                            stringResource(R.string.unwatch)
                        } else {
                            stringResource(R.string.watched)
                        }
                    )
                }

                if (movie.isSeries) {
                    Button(
                        onClick = {
                            movieViewModel.setWatched(movie, !movie.watched)
                        }
                    ) {
                        Text(
                            if (movie.watched) {
                                stringResource(R.string.reopen_series)
                            } else {
                                stringResource(R.string.complete_series)
                            }
                        )
                    }
                }

                if (movie.watched &&
                    movie.collection != CollectionNames.WATCHED
                ) {
                    Button(
                        onClick = {
                            val previousCollection = movie.collection
                            movieViewModel.moveMovie(movie, CollectionNames.WATCHED)
                            val movedToWatchedMessage = resources.getString(
                                R.string.moved_to_collection,
                                movie.title,
                                CollectionNames.WATCHED
                            )
                            onShowUndo(movedToWatchedMessage) {
                                movieViewModel.moveMovie(movie, previousCollection)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.to_watched))
                    }
                }

                Button(
                    onClick = {
                        onEdit(movie)
                    }
                ) {

                    Text(stringResource(R.string.edit))
                }

                Button(
                    onClick = {
                        onDelete(movie)
                    }
                ) {
                    Text(stringResource(R.string.move_to_trash))
                }
            }
        }
    }
}

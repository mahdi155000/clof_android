package com.mahdi155000.clof_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)

        setContent {

            var darkMode by remember {
                mutableStateOf(
                    settingsManager.isDarkMode()
                )
            }

            MaterialTheme(
                colorScheme = if (darkMode) {
                    androidx.compose.material3.darkColorScheme()
                } else {
                    androidx.compose.material3.lightColorScheme()
                }
            ) {

                ClofApp(
                    darkMode = darkMode,
                    onDarkModeChange = { enabled ->
                        darkMode = enabled
                        settingsManager.setDarkMode(enabled)
                    }
                )
            }
        }
    }
}


@Composable
fun ClofApp(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    movieViewModel: MovieViewModel = viewModel()
) {
    var showAddMovieScreen by remember {
        mutableStateOf(false)
    }

    var movieBeingEdited by remember {
        mutableStateOf<MovieEntity?>(null)
    }

    var movieBeingViewed by remember {
        mutableStateOf<MovieEntity?>(null)
    }

    if (movieBeingViewed != null) {

        val movie = movieBeingViewed!!

        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                MovieDetailsScreen(
                    movie = movie,
                    movieViewModel = movieViewModel,
                    onBack = {
                        movieBeingViewed = null
                    },
                    onEdit = {
                        movieBeingViewed = null
                        movieBeingEdited = movie
                    }
                )
            }
        }

    } else if (movieBeingEdited != null) {
        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Button(
                    onClick = {
                        showAddMovieScreen = false
                    },
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Back")
                }

                AddMovieScreen(
                    movieViewModel = movieViewModel,
                    onMovieAdded = {
                        showAddMovieScreen = false
                    }
                )
            }
        }

    } else {

        val movies by movieViewModel.movies.collectAsState()

        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        showAddMovieScreen = true
                    }
                ) {
                    Text("+")
                }
            }
        ) { innerPadding ->

            MovieList(
                movies = movies,
                movieViewModel = movieViewModel,
                onEdit = { movie ->
                    movieBeingEdited = movie
                },
                onMovieClick = { movie ->
                    movieBeingViewed = movie
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClofTopBar(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Text("CLOF")
        },
        actions = {

            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text("Dark")

                Switch(
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange
                )
            }
        }
    )
}


@Composable
fun MovieList(
    movies: List<MovieEntity>,
    movieViewModel: MovieViewModel,
    onEdit: (MovieEntity) -> Unit,
    onMovieClick: (MovieEntity) -> Unit,
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

    var sortOption by remember {
        mutableStateOf("Recently Added")
    }

    var showFilters by remember {
        mutableStateOf(false)
    }

    val filteredMovies = movies
        .filter { movie ->

            val matchesSearch =
                movie.title.contains(
                    searchText,
                    ignoreCase = true
                ) ||
                        movie.genre.contains(
                            searchText,
                            ignoreCase = true
                        )

            val matchesType =
                when (typeFilter) {
                    "Movies" -> !movie.isSeries
                    "Series" -> movie.isSeries
                    else -> true
                }

            val matchesWatched =
                when (watchedFilter) {
                    "Watched" -> movie.watched
                    "Unwatched" -> !movie.watched
                    else -> true
                }

            matchesSearch &&
                    matchesType &&
                    matchesWatched
        }
        .let { list ->

            when (sortOption) {

                "Title A-Z" -> {
                    list.sortedBy {
                        it.title.lowercase()
                    }
                }

                "Title Z-A" -> {
                    list.sortedByDescending {
                        it.title.lowercase()
                    }
                }

                "Oldest Added" -> {
                    list.sortedBy {
                        it.id
                    }
                }

                else -> {
                    list.sortedByDescending {
                        it.id
                    }
                }
            }
        }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // Search
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
                Text("Search")
            },
            placeholder = {
                Text("Search movies and series")
            },
            singleLine = true
        )

        // Filter button
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
                    "Hide Filters"
                } else {
                    "Show Filters"
                }
            )
        }

        if (showFilters) {

            // Type filter
            Text(
                text = "Type",
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
                    text = "All",
                    selected = typeFilter == "All"
                ) {
                    typeFilter = "All"
                }

                FilterButton(
                    text = "Movies",
                    selected = typeFilter == "Movies"
                ) {
                    typeFilter = "Movies"
                }

                FilterButton(
                    text = "Series",
                    selected = typeFilter == "Series"
                ) {
                    typeFilter = "Series"
                }
            }

            // Watched filter
            Text(
                text = "Watched",
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
                    text = "All",
                    selected = watchedFilter == "All"
                ) {
                    watchedFilter = "All"
                }

                FilterButton(
                    text = "Watched",
                    selected = watchedFilter == "Watched"
                ) {
                    watchedFilter = "Watched"
                }

                FilterButton(
                    text = "Unwatched",
                    selected = watchedFilter == "Unwatched"
                ) {
                    watchedFilter = "Unwatched"
                }
            }

            // Sort
            Text(
                text = "Sort",
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
                    text = "Recent",
                    selected = sortOption == "Recently Added"
                ) {
                    sortOption = "Recently Added"
                }

                FilterButton(
                    text = "Oldest",
                    selected = sortOption == "Oldest Added"
                ) {
                    sortOption = "Oldest Added"
                }

                FilterButton(
                    text = "A-Z",
                    selected = sortOption == "Title A-Z"
                ) {
                    sortOption = "Title A-Z"
                }

                FilterButton(
                    text = "Z-A",
                    selected = sortOption == "Title Z-A"
                ) {
                    sortOption = "Title Z-A"
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        // Movie list
        if (filteredMovies.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = if (movies.isEmpty()) {
                        "No movies yet"
                    } else {
                        "No results found"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = if (movies.isEmpty()) {
                        "Press + to add a movie."
                    } else {
                        "Try changing your search or filters."
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
                        onEdit = onEdit,
                        onMovieClick = onMovieClick
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }
        }
    }
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
            text = if (selected) {
                "✓ $text"
            } else {
                text
            }
        )
    }
}


@Composable
fun MovieItem(
    movie: MovieEntity,
    movieViewModel: MovieViewModel,
    onEdit: (MovieEntity) -> Unit,
    onMovieClick: (MovieEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            onMovieClick(movie)
        },
        modifier = modifier.fillMaxWidth()
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
                    "Series"
                } else {
                    "Movie"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            if (movie.isSeries) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Season ${movie.season} • Episode ${movie.episode}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = if (movie.watched) {
                    "Watched"
                } else {
                    "Not watched"
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        movieViewModel.setWatched(
                            movie,
                            !movie.watched
                        )
                    }
                ) {
                    Text(
                        text = if (movie.watched) {
                            "Unwatch"
                        } else {
                            "Watched"
                        }
                    )
                }

                Button(
                    onClick = {
                        onEdit(movie)
                    }
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = {
                        movieViewModel.deleteMovie(movie)
                    }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
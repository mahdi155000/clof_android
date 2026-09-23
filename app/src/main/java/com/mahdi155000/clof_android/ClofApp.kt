package com.mahdi155000.clof_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

@Composable
fun ClofApp(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    movieViewModel: MovieViewModel = viewModel()
) {
    var showAddMovieScreen by remember {
        mutableStateOf(false)
    }

    var movieBeingViewed by remember {
        mutableStateOf<MovieEntity?>(null)
    }

    var movieBeingEdited by remember {
        mutableStateOf<MovieEntity?>(null)
    }

    var showCollectionsScreen by remember { mutableStateOf(false) }
    var showMoveMoviesScreen by remember { mutableStateOf(false) }
    var showGenresScreen by remember { mutableStateOf(false) }
    var showTrashScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var movieToDelete by remember { mutableStateOf<MovieEntity?>(null) }

    var isImporting by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImporting = true
            movieViewModel.importClofDatabase(
                uri = uri,
                onComplete = { result ->
                    isImporting = false
                    importMessage = "Imported ${result.imported} items. " +
                        "Skipped ${result.skipped} duplicate items."
                },
                onError = { error ->
                    isImporting = false
                    importMessage = "Import failed: ${error.message ?: "invalid Clof database"}"
                }
            )
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            isExporting = true
            movieViewModel.exportClofDatabase(
                uri = uri,
                onComplete = {
                    isExporting = false
                    importMessage = "Database exported successfully."
                },
                onError = { error ->
                    isExporting = false
                    importMessage = "Export failed: ${error.message ?: "unable to write database"}"
                }
            )
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { drawerScope.launch { drawerState.open() } }
    val closeDrawer: () -> Unit = { drawerScope.launch { drawerState.close() } }
    val collections by movieViewModel.collections.collectAsState()
    val movies by movieViewModel.movies.collectAsState()
    var selectedCollection by remember {
        mutableStateOf<String?>(CollectionNames.MAIN)
    }
    var collectionsExpanded by remember { mutableStateOf(true) }

    fun collectionSummary(collectionName: String?): String {
        val matchingMovies = if (collectionName == null) {
            movies
        } else {
            movies.filter { it.collection == collectionName }
        }
        val watched = matchingMovies.count { it.watched }
        return "${matchingMovies.size} (${watched} watched, ${matchingMovies.size - watched} unwatched)"
    }

    val onImport = {
        if (!isImporting && !isExporting) {
            importLauncher.launch(arrayOf("*/*"))
        }
    }
    val onExport = {
        if (!isImporting && !isExporting) {
            exportLauncher.launch("clof-export.db")
        }
    }
    val showUndoSnackbar: (String, () -> Unit) -> Unit = { message, undo ->
        drawerScope.launch {
            if (snackbarHostState.showSnackbar(message, "Undo") == SnackbarResult.ActionPerformed) {
                undo()
            }
        }
    }

    importMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { importMessage = null },
            confirmButton = {
                Button(onClick = { importMessage = null }) {
                    Text("OK")
                }
            },
            title = { Text("Clof import") },
            text = { Text(message) }
        )
    }

    BackHandler(
        enabled = showAddMovieScreen || movieBeingViewed != null ||
            movieBeingEdited != null || showCollectionsScreen ||
            showMoveMoviesScreen || showGenresScreen || showTrashScreen ||
                showSettingsScreen || selectedCollection != null
    ) {
        when {
            showCollectionsScreen -> showCollectionsScreen = false
            showMoveMoviesScreen -> showMoveMoviesScreen = false
            showGenresScreen -> showGenresScreen = false
            showTrashScreen -> showTrashScreen = false
            showSettingsScreen -> showSettingsScreen = false
            movieBeingViewed != null -> movieBeingViewed = null
            movieBeingEdited != null -> movieBeingEdited = null
            showAddMovieScreen -> showAddMovieScreen = false
            selectedCollection != null -> selectedCollection = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = { Text("All collections (${collectionSummary(null)})") },
                    selected = selectedCollection == null &&
                        !showCollectionsScreen &&
                        !showMoveMoviesScreen,
                    onClick = {
                        selectedCollection = null
                        showAddMovieScreen = false
                        movieBeingViewed = null
                        movieBeingEdited = null
                        showCollectionsScreen = false
                        showMoveMoviesScreen = false
                        showGenresScreen = false
                        showTrashScreen = false
                        showSettingsScreen = false
                        closeDrawer()
                    }
                )

                if (collectionsExpanded) {
                    collections.forEach { collection ->
                        NavigationDrawerItem(
                            label = {
                                Text("  $collection (${collectionSummary(collection)})")
                            },
                            selected = selectedCollection == collection &&
                                !showCollectionsScreen &&
                                !showMoveMoviesScreen,
                            onClick = {
                                selectedCollection = collection
                                showAddMovieScreen = false
                                movieBeingViewed = null
                                movieBeingEdited = null
                                showCollectionsScreen = false
                                showMoveMoviesScreen = false
                                showGenresScreen = false
                                showTrashScreen = false
                                showSettingsScreen = false
                                closeDrawer()
                            }
                        )
                    }
                }

                NavigationDrawerItem(
                    label = { Text("Manage collections") },
                    selected = showCollectionsScreen,
                    onClick = {
                        showAddMovieScreen = false
                        movieBeingViewed = null
                        movieBeingEdited = null
                        showCollectionsScreen = true
                        showMoveMoviesScreen = false
                        showGenresScreen = false
                        showTrashScreen = false
                        showSettingsScreen = false
                        closeDrawer()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Move movies / series") },
                    selected = showMoveMoviesScreen,
                    onClick = {
                        showAddMovieScreen = false
                        movieBeingViewed = null
                        movieBeingEdited = null
                        showMoveMoviesScreen = true
                        showCollectionsScreen = false
                        showGenresScreen = false
                        showTrashScreen = false
                        showSettingsScreen = false
                        closeDrawer()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Manage genres") },
                    selected = showGenresScreen,
                    onClick = {
                        showAddMovieScreen = false
                        movieBeingViewed = null
                        movieBeingEdited = null
                        showGenresScreen = true
                        showCollectionsScreen = false
                        showMoveMoviesScreen = false
                        showTrashScreen = false
                        closeDrawer()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Trash") },
                    selected = showTrashScreen,
                    onClick = {
                        showAddMovieScreen = false
                        movieBeingViewed = null
                        movieBeingEdited = null
                        selectedCollection = null
                        showTrashScreen = true
                        showGenresScreen = false
                        showCollectionsScreen = false
                        showMoveMoviesScreen = false
                        showSettingsScreen = false
                        closeDrawer()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = showSettingsScreen,
                    onClick = {
                        showAddMovieScreen = false
                        movieBeingViewed = null
                        movieBeingEdited = null
                        selectedCollection = null
                        showSettingsScreen = true
                        showTrashScreen = false
                        showGenresScreen = false
                        showCollectionsScreen = false
                        showMoveMoviesScreen = false
                        closeDrawer()
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        if (showSettingsScreen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = {},
                        onOpenDrawer = openDrawer
                    )
                }
            ) { innerPadding ->
                SettingsScreen(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onImport = onImport,
                    isImporting = isImporting,
                    onExport = onExport,
                    isExporting = isExporting,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            return@ModalNavigationDrawer
        }

        if (showCollectionsScreen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CollectionsScreen(
                        movieViewModel = movieViewModel,
                        onBack = { showCollectionsScreen = false },
                        onCollectionRenamed = { oldName, newName ->
                            if (selectedCollection == oldName) {
                                selectedCollection = newName
                            }
                        },
                        onCollectionRemoved = { removedName ->
                            if (selectedCollection == removedName) {
                                selectedCollection = null
                            }
                        },
                        onViewCollection = { collection ->
                            selectedCollection = collection
                            showCollectionsScreen = false
                        }
                    )
                }
            }
            return@ModalNavigationDrawer
        }

        if (showGenresScreen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    GenresScreen(
                        movieViewModel = movieViewModel,
                        onBack = { showGenresScreen = false },
                        onShowUndo = showUndoSnackbar
                    )
                }
            }
            return@ModalNavigationDrawer
        }

        if (showTrashScreen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TrashScreen(
                        movieViewModel = movieViewModel,
                        onBack = { showTrashScreen = false }
                    )
                }
            }
            return@ModalNavigationDrawer
        }

        if (movieBeingViewed != null) {
            val movie = movieBeingViewed!!

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MovieDetailsScreen(
                        movieId = movie.id,
                        movieViewModel = movieViewModel,
                        onBack = {
                            movieBeingViewed = null
                        },
                        onEdit = { currentMovie ->
                            movieBeingEdited = currentMovie
                            movieBeingViewed = null
                        },
                        onShowUndo = showUndoSnackbar
                    )
                }
            }

            return@ModalNavigationDrawer
        }

        if (movieBeingEdited != null) {
            val movie = movieBeingEdited!!

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
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
                            movieBeingEdited = null
                        },
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("Back")
                    }

                    EditMovieScreen(
                        movie = movie,
                        movieViewModel = movieViewModel,
                        onMovieUpdated = {
                            movieBeingEdited = null
                        }
                    )
                }
            }

            return@ModalNavigationDrawer
        }

        if (showAddMovieScreen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
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

            return@ModalNavigationDrawer
        }

        if (showMoveMoviesScreen) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    ClofTopBar(
                        darkMode = darkMode,
                        onDarkModeChange = onDarkModeChange,
                        onImport = onImport,
                        isImporting = isImporting,
                        onExport = onExport,
                        isExporting = isExporting,
                        onManageCollections = { showCollectionsScreen = true },
                        onOpenDrawer = openDrawer
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MoveMoviesScreen(
                        movieViewModel = movieViewModel,
                        onBack = { showMoveMoviesScreen = false },
                        onShowUndo = showUndoSnackbar
                    )
                }
            }
            return@ModalNavigationDrawer
        }

        movieToDelete?.let { movie ->
            AlertDialog(
                onDismissRequest = { movieToDelete = null },
                title = { Text("Move to trash?") },
                text = { Text("\"${movie.title}\" can be restored from Trash.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val previous = movie
                            movieViewModel.deleteMovie(movie)
                            movieToDelete = null
                            showUndoSnackbar("Moved \"${movie.title}\" to trash") {
                                movieViewModel.restoreMovie(previous)
                            }
                        }
                    ) {
                        Text("Move to Trash")
                    }
                },
                dismissButton = {
                    Button(onClick = { movieToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                ClofTopBar(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onImport = onImport,
                    isImporting = isImporting,
                    onExport = onExport,
                    isExporting = isExporting,
                    onManageCollections = { showCollectionsScreen = true },
                    onOpenDrawer = openDrawer
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
                onMovieClick = { movie ->
                    movieBeingViewed = movie
                },
                onEdit = { movie ->
                    movieBeingEdited = movie
                },
                onDelete = { movieToDelete = it },
                selectedCollection = selectedCollection,
                collectionNames = collections,
                onShowUndo = showUndoSnackbar,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
        }
    }
}


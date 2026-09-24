package com.mahdi155000.clof_android

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.data.MovieEntity
import com.mahdi155000.clof_android.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

private object ClofRoutes {
    const val HOME = "home/{collection}"
    const val HOME_BASE = "home"
    const val ADD_MOVIE = "add-movie"
    const val COLLECTIONS = "collections"
    const val MOVE_MOVIES = "move-movies"
    const val GENRES = "genres"
    const val TRASH = "trash"
    const val SETTINGS = "settings"
    const val MOVIE_DETAILS = "movie/{movieId}"
    const val EDIT_MOVIE = "movie/{movieId}/edit"

    fun home(collection: String?) =
        "$HOME_BASE/${Uri.encode(collection ?: "all")}"

    fun movieDetails(movieId: Int) = "movie/$movieId"
    fun editMovie(movieId: Int) = "movie/$movieId/edit"
}

@Composable
fun ClofApp(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    movieViewModel: MovieViewModel = viewModel()
) {
    val navController = rememberNavController()
    val importedItemsFormat = stringResource(R.string.imported_items)
    val importFailedFormat = stringResource(R.string.import_failed)
    val invalidDatabaseMessage = stringResource(R.string.invalid_clof_database)
    val databaseExportedMessage = stringResource(R.string.database_exported)
    val exportFailedFormat = stringResource(R.string.export_failed)
    val unableToWriteDatabaseMessage = stringResource(R.string.unable_to_write_database)
    val collectionSummaryFormat = stringResource(R.string.collection_summary)
    val undoLabel = stringResource(R.string.undo)
    val moveToTrashSnackbarFormat = stringResource(R.string.move_to_trash_snackbar)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val collections by movieViewModel.collections.collectAsState()
    val movies by movieViewModel.movies.collectAsState()
    var collectionsExpanded by rememberSaveable { mutableStateOf(true) }
    var selectedCollection by rememberSaveable { mutableStateOf<String?>(CollectionNames.MAIN) }
    var movieToDelete by remember { mutableStateOf<MovieEntity?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var importMessage by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImporting = true
            movieViewModel.importClofDatabase(
                uri = uri,
                onComplete = { result ->
                    isImporting = false
                    importMessage = String.format(
                        Locale.getDefault(),
                        importedItemsFormat,
                        result.imported,
                        result.skipped
                    )
                },
                onError = { error ->
                    isImporting = false
                    importMessage = String.format(
                        Locale.getDefault(),
                        importFailedFormat,
                        error.message ?: invalidDatabaseMessage
                    )
                }
            )
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            isExporting = true
            movieViewModel.exportClofDatabase(
                uri = uri,
                onComplete = {
                    isExporting = false
                    importMessage = databaseExportedMessage
                },
                onError = { error ->
                    isExporting = false
                    importMessage = String.format(
                        Locale.getDefault(),
                        exportFailedFormat,
                        error.message ?: unableToWriteDatabaseMessage
                    )
                }
            )
        }
    }

    fun collectionSummary(collectionName: String?): String {
        val matchingMovies = if (collectionName == null) {
            movies
        } else {
            movies.filter { it.collection == collectionName }
        }
        val watched = matchingMovies.count { it.watched }
        return String.format(
            Locale.getDefault(),
            collectionSummaryFormat,
            matchingMovies.size,
            watched,
            matchingMovies.size - watched
        )
    }

    val onImport = {
        if (!isImporting && !isExporting) importLauncher.launch(arrayOf("*/*"))
    }
    val onExport = {
        if (!isImporting && !isExporting) exportLauncher.launch("clof-export.db")
    }
    val showUndoSnackbar: (String, () -> Unit) -> Unit = { message, undo ->
        scope.launch {
            if (snackbarHostState.showSnackbar(
                    message,
                    undoLabel
                ) == SnackbarResult.ActionPerformed
            ) {
                undo()
            }
        }
    }
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }
    val navigateFromDrawer: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(ClofRoutes.home(CollectionNames.MAIN)) { inclusive = false }
            launchSingleTop = true
        }
        closeDrawer()
    }

    importMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { importMessage = null },
            confirmButton = {
                Button(onClick = { importMessage = null }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.clof_import)) },
            text = { Text(message) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.menu),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                val currentRoute by navController.currentBackStackEntryFlow
                    .collectAsState(initial = navController.currentBackStackEntry)
                val currentDestination = currentRoute?.destination
                val routeCollection = currentRoute?.arguments?.getString("collection")
                    ?.takeUnless { it == "all" }
                    ?.let(Uri::decode)

                NavigationDrawerItem(
                    label = {
                        Text(stringResource(R.string.all_collections, collectionSummary(null)))
                    },
                    selected = currentDestination?.route == ClofRoutes.HOME &&
                        routeCollection == null,
                    onClick = {
                        selectedCollection = null
                        navigateFromDrawer(ClofRoutes.home(null))
                    }
                )
                if (collectionsExpanded) {
                    collections.forEach { collection ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    stringResource(
                                        R.string.collection_item,
                                        collection,
                                        collectionSummary(collection)
                                    )
                                )
                            },
                            selected = currentDestination?.route == ClofRoutes.HOME &&
                                routeCollection == collection,
                            onClick = {
                                selectedCollection = collection
                                navigateFromDrawer(ClofRoutes.home(collection))
                            }
                        )
                    }
                }
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.manage_collections)) },
                    selected = currentDestination?.route == ClofRoutes.COLLECTIONS,
                    onClick = { navigateFromDrawer(ClofRoutes.COLLECTIONS) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.move_movies_series)) },
                    selected = currentDestination?.route == ClofRoutes.MOVE_MOVIES,
                    onClick = { navigateFromDrawer(ClofRoutes.MOVE_MOVIES) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.manage_genres)) },
                    selected = currentDestination?.route == ClofRoutes.GENRES,
                    onClick = { navigateFromDrawer(ClofRoutes.GENRES) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.trash)) },
                    selected = currentDestination?.route == ClofRoutes.TRASH,
                    onClick = { navigateFromDrawer(ClofRoutes.TRASH) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings)) },
                    selected = currentDestination?.route == ClofRoutes.SETTINGS,
                    onClick = { navigateFromDrawer(ClofRoutes.SETTINGS) }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = ClofRoutes.home(CollectionNames.MAIN)
        ) {
            composable(
                route = ClofRoutes.HOME,
                arguments = listOf(navArgument("collection") { type = NavType.StringType })
            ) { entry ->
                val collection = entry.arguments?.getString("collection")
                    ?.takeUnless { it == "all" }
                    ?.let(Uri::decode)
                LaunchedEffect(collection) {
                    selectedCollection = collection
                }
                ClofScaffold(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onImport = onImport,
                    isImporting = isImporting,
                    onExport = onExport,
                    isExporting = isExporting,
                    onOpenDrawer = openDrawer,
                    snackbarHostState = snackbarHostState,
                    floatingActionButton = {
                        androidx.compose.material3.FloatingActionButton(
                            onClick = { navController.navigate(ClofRoutes.ADD_MOVIE) }
                        ) { Text(stringResource(R.string.add)) }
                    }
                ) { padding ->
                    MovieList(
                        movies = movies,
                        movieViewModel = movieViewModel,
                        onMovieClick = { navController.navigate(ClofRoutes.movieDetails(it.id)) },
                        onEdit = { navController.navigate(ClofRoutes.editMovie(it.id)) },
                        onDelete = { movieToDelete = it },
                        selectedCollection = collection,
                        collectionNames = collections,
                        onShowUndo = showUndoSnackbar,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
            }
            composable(ClofRoutes.ADD_MOVIE) {
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        BackButton { navController.popBackStack() }
                        AddMovieScreen(movieViewModel) { navController.popBackStack() }
                    }
                }
            }
            composable(ClofRoutes.COLLECTIONS) {
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        CollectionsScreen(
                            movieViewModel = movieViewModel,
                            onBack = { navController.popBackStack() },
                            onCollectionRenamed = { oldName, newName ->
                                if (selectedCollection == oldName) {
                                    navController.navigate(ClofRoutes.home(newName)) {
                                        popUpTo(ClofRoutes.COLLECTIONS) { inclusive = true }
                                    }
                                }
                            },
                            onCollectionRemoved = { removedName ->
                                if (selectedCollection == removedName) {
                                    navController.navigate(ClofRoutes.home(null)) {
                                        popUpTo(ClofRoutes.COLLECTIONS) { inclusive = true }
                                    }
                                }
                            },
                            onViewCollection = { collection ->
                                navController.navigate(ClofRoutes.home(collection)) {
                                    popUpTo(ClofRoutes.COLLECTIONS) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
            composable(ClofRoutes.GENRES) {
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        GenresScreen(movieViewModel, { navController.popBackStack() }, showUndoSnackbar)
                    }
                }
            }
            composable(ClofRoutes.TRASH) {
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        TrashScreen(movieViewModel) { navController.popBackStack() }
                    }
                }
            }
            composable(ClofRoutes.MOVE_MOVIES) {
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        MoveMoviesScreen(movieViewModel, { navController.popBackStack() }, showUndoSnackbar)
                    }
                }
            }
            composable(ClofRoutes.SETTINGS) {
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    SettingsScreen(
                        darkMode, onDarkModeChange, onImport, isImporting,
                        onExport, isExporting, Modifier.padding(padding)
                    )
                }
            }
            composable(
                route = ClofRoutes.MOVIE_DETAILS,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { entry ->
                val movieId = entry.arguments?.getInt("movieId") ?: return@composable
                ClofScaffoldContent(
                    darkMode, onDarkModeChange, onImport, isImporting, onExport,
                    isExporting, openDrawer, snackbarHostState
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        MovieDetailsScreen(
                            movieId = movieId,
                            movieViewModel = movieViewModel,
                            onBack = { navController.popBackStack() },
                            onEdit = {
                                navController.navigate(ClofRoutes.editMovie(it.id)) {
                                    popUpTo(ClofRoutes.movieDetails(movieId)) { inclusive = true }
                                }
                            },
                            onShowUndo = showUndoSnackbar
                        )
                    }
                }
            }
            composable(
                route = ClofRoutes.EDIT_MOVIE,
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { entry ->
                val movieId = entry.arguments?.getInt("movieId") ?: return@composable
                val movie by movieViewModel.observeMovie(movieId).collectAsState(initial = null)
                movie?.let { currentMovie ->
                    ClofScaffoldContent(
                        darkMode, onDarkModeChange, onImport, isImporting, onExport,
                        isExporting, openDrawer, snackbarHostState
                    ) { padding ->
                        Column(Modifier.fillMaxSize().padding(padding)) {
                            BackButton { navController.popBackStack() }
                            EditMovieScreen(
                                movie = currentMovie,
                                movieViewModel = movieViewModel,
                                onMovieUpdated = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    movieToDelete?.let { movie ->
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            title = { Text(stringResource(R.string.move_to_trash_title)) },
            text = { Text(stringResource(R.string.restorable_from_trash, movie.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        movieViewModel.deleteMovie(movie)
                        movieToDelete = null
                        showUndoSnackbar(
                            String.format(
                                Locale.getDefault(),
                                moveToTrashSnackbarFormat,
                                movie.title
                            )
                        ) {
                            movieViewModel.restoreMovie(movie)
                        }
                    }
                ) { Text(stringResource(R.string.move_to_trash)) }
            },
            dismissButton = {
                Button(onClick = { movieToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ClofScaffoldContent(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onImport: () -> Unit,
    isImporting: Boolean,
    onExport: () -> Unit,
    isExporting: Boolean,
    onOpenDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ClofTopBar(
                darkMode, onDarkModeChange, onImport, isImporting,
                onExport, isExporting, {}, onOpenDrawer
            )
        },
        content = content
    )
}

@Composable
private fun ClofScaffold(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onImport: () -> Unit,
    isImporting: Boolean,
    onExport: () -> Unit,
    isExporting: Boolean,
    onOpenDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ClofTopBar(
                darkMode, onDarkModeChange, onImport, isImporting,
                onExport, isExporting, {}, onOpenDrawer
            )
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(12.dp)) {
        Text(stringResource(R.string.back))
    }
}

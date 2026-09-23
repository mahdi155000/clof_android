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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahdi155000.clof_android.data.CollectionNames
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun CollectionsScreen(
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onCollectionRenamed: (oldName: String, newName: String) -> Unit = { _, _ -> },
    onCollectionRemoved: (name: String) -> Unit = {},
    onViewCollection: (name: String) -> Unit = {}
) {
    val collections by movieViewModel.collections.collectAsState()
    var newCollectionName by remember { mutableStateOf("") }
    var editingCollection by remember { mutableStateOf<String?>(null) }
    var editedName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var collectionToRemove by remember { mutableStateOf<String?>(null) }
    val movies by movieViewModel.movies.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Manage collections")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newCollectionName,
            onValueChange = { newCollectionName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New collection") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val name = newCollectionName.trim()
                if (name.isNotEmpty()) {
                    movieViewModel.addCollection(name) { added ->
                        message = if (added) {
                            newCollectionName = ""
                            "Collection added."
                        } else {
                            "That collection already exists."
                        }
                    }
                }
            },
            enabled = newCollectionName.isNotBlank()
        ) {
            Text("Add collection")
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(collections, key = { it }) { collection ->
                if (editingCollection == collection) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Collection name") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Button(
                                onClick = {
                                    val name = editedName.trim()
                                    if (name.isNotEmpty()) {
                                        movieViewModel.renameCollection(collection, name) { renamed ->
                                            message = if (renamed) {
                                                editingCollection = null
                                                onCollectionRenamed(collection, name)
                                                "Collection renamed."
                                            } else {
                                                "Use a new collection name."
                                            }
                                        }
                                    }
                                },
                                enabled = editedName.isNotBlank()
                            ) {
                                Text("Save")
                            }
                        }
                        item {
                            Button(onClick = { editingCollection = null }) {
                                Text("Cancel")
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(collection, modifier = Modifier.padding(top = 12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Button(
                                    onClick = { onViewCollection(collection) }
                                ) {
                                    Text("View")
                                }
                            }
                            item {
                                Button(
                                    onClick = {
                                        editingCollection = collection
                                        editedName = collection
                                    },
                                    enabled = !CollectionNames.isReserved(collection)
                                ) {
                                    Text("Rename")
                                }
                            }
                            item {
                                Button(
                                    onClick = {
                                        collectionToRemove = collection
                                    },
                                    enabled = !CollectionNames.isReserved(collection)
                                ) {
                                    Text("Remove")
                                }
                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        collectionToRemove?.let { collection ->
            val affectedMovies = movies.filter { it.collection == collection }
            AlertDialog(
                onDismissRequest = { collectionToRemove = null },
                title = { Text("Remove collection?") },
                text = {
                    Text(
                        if (affectedMovies.isEmpty()) {
                            "No movies or series are in \"$collection\"."
                        } else {
                            val titles = affectedMovies.joinToString("\n") { "• ${it.title}" }
                            "The ${affectedMovies.size} movie(s)/series in \"$collection\" " +
                                "will be moved to \"${CollectionNames.MAIN}\":\n\n$titles"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            collectionToRemove = null
                            movieViewModel.removeCollection(collection) { removed ->
                                message = if (removed) {
                                    onCollectionRemoved(collection)
                                    "Collection removed; its movies moved to ${CollectionNames.MAIN}."
                                } else {
                                    "Reserved collections cannot be removed."
                                }
                            }
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    Button(onClick = { collectionToRemove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

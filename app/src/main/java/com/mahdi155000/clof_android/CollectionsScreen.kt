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
import com.mahdi155000.clof_android.viewmodel.MovieViewModel

@Composable
fun CollectionsScreen(
    movieViewModel: MovieViewModel,
    onBack: () -> Unit,
    onCollectionRenamed: (oldName: String, newName: String) -> Unit = { _, _ -> },
    onCollectionRemoved: (name: String) -> Unit = {}
) {
    val collections by movieViewModel.collections.collectAsState()
    var newCollectionName by remember { mutableStateOf("") }
    var editingCollection by remember { mutableStateOf<String?>(null) }
    var editedName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Button(onClick = { editingCollection = null }) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(collection, modifier = Modifier.padding(top = 12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    editingCollection = collection
                                    editedName = collection
                                },
                                enabled = collection != "main"
                            ) {
                                Text("Rename")
                            }
                            Button(
                                onClick = {
                                    movieViewModel.removeCollection(collection) { removed ->
                                        message = if (removed) {
                                            onCollectionRemoved(collection)
                                            "Collection removed; its movies moved to main."
                                        } else {
                                            "The main collection cannot be removed."
                                        }
                                    }
                                },
                                enabled = collection != "main"
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

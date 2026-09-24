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
import androidx.compose.ui.res.stringResource

@Composable
fun CollectionPicker(
    selectedCollection: String,
    collections: List<String>,
    onCollectionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Button(onClick = { expanded = true }) {
        Text(stringResource(R.string.collection_label, selectedCollection))
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        collections.forEach { collection ->
            DropdownMenuItem(
                text = { Text(collection) },
                onClick = {
                    onCollectionSelected(collection)
                    expanded = false
                }
            )
        }
    }
}

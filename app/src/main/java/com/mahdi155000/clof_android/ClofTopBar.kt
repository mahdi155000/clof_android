package com.mahdi155000.clof_android

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClofTopBar(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onImport: () -> Unit,
    isImporting: Boolean,
    onExport: () -> Unit,
    isExporting: Boolean,
    onManageCollections: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    TopAppBar(
        title = { Text("CLOF") },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Text("☰")
            }
        },
        actions = {}
    )
}

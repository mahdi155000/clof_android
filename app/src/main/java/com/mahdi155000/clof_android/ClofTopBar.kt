package com.mahdi155000.clof_android

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

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
    var menuExpanded by remember { mutableStateOf(false) }
    val actionsEnabled = !isImporting && !isExporting

    TopAppBar(
        title = { Text(stringResource(R.string.clof_title)) },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Text(stringResource(R.string.menu_icon))
            }
        },
        actions = {
            IconButton(
                onClick = { menuExpanded = true },
                enabled = actionsEnabled
            ) {
                Text(
                    text = stringResource(R.string.more_icon),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_database_menu)) },
                    enabled = actionsEnabled,
                    onClick = {
                        menuExpanded = false
                        onImport()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_database_menu)) },
                    enabled = actionsEnabled,
                    onClick = {
                        menuExpanded = false
                        onExport()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.manage_collections)) },
                    onClick = {
                        menuExpanded = false
                        onManageCollections()
                    }
                )
            }
        }
    )
}

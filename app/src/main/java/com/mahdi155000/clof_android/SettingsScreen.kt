package com.mahdi155000.clof_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onImport: () -> Unit,
    isImporting: Boolean,
    onExport: () -> Unit,
    isExporting: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.dark_mode))
            Switch(
                checked = darkMode,
                onCheckedChange = onDarkModeChange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.database_backup),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.backup_description))
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onExport,
            enabled = !isImporting && !isExporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (isExporting) R.string.exporting else R.string.export_database))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onImport,
            enabled = !isImporting && !isExporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (isImporting) R.string.importing else R.string.import_database))
        }
    }
}

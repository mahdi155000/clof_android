package com.mahdi155000.clof_android.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ClofDatabaseExporter(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val databaseFile = context.getDatabasePath("clof.db")
        if (!databaseFile.exists()) {
            throw IOException("The Clof database does not exist yet.")
        }

        database.openHelper.writableDatabase.query(
            "PRAGMA wal_checkpoint(FULL)"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                // Consume the checkpoint result before copying the database file.
            }
        }

        context.contentResolver.openOutputStream(uri)?.use { output ->
            databaseFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to write the exported database file.")
    }
}

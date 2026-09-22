package com.mahdi155000.clof_android.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

data class ImportResult(
    val imported: Int,
    val skipped: Int
)

/** Imports the `movies.db` SQLite database created by the Clof terminal application. */
class ClofDatabaseImporter(
    private val context: Context,
    private val repository: MovieRepository,
    private val collectionRepository: CollectionRepository,
    private val genreRepository: GenreRepository
) {
    suspend fun importFrom(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val temporaryDatabase = File(
            context.cacheDir,
            "clof-import-${UUID.randomUUID()}.db"
        )

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temporaryDatabase.outputStream().use(input::copyTo)
            } ?: throw IOException("Unable to read the selected database file.")

            val importedRecords = readMovies(temporaryDatabase)
            val importedMovies = deduplicateMoviesByTitle(importedRecords)
            collectionRepository.addMissingCollections(importedMovies.map { it.collection }.toSet())
            genreRepository.addMissingGenres(
                importedMovies
                    .flatMap { it.genre.split(",") }
                    .map { it.trim() }
                    .toSet()
            )
            val inserted = repository.insertMissingMovies(importedMovies)
            ImportResult(
                imported = inserted,
                skipped = importedRecords.size - importedMovies.size + importedMovies.size - inserted
            )
        } finally {
            temporaryDatabase.delete()
        }
    }

    private fun readMovies(databaseFile: File): List<MovieEntity> {
        val database = SQLiteDatabase.openDatabase(
            databaseFile.path,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        return try {
            database.rawQuery("SELECT * FROM movies ORDER BY id", null).use { cursor ->
                val titleColumn = cursor.requiredColumn("title")
                val createdAtColumn = cursor.optionalColumn("created_at", "createdAt")
                val genreColumn = cursor.optionalColumn("genre")
                val seriesColumn = cursor.optionalColumn("is_series", "isSeries")
                val seasonColumn = cursor.optionalColumn("season")
                val episodeColumn = cursor.optionalColumn("episode")
                val watchedColumn = cursor.optionalColumn("watched")
                val collectionColumn = cursor.optionalColumn("collection")
                val movies = mutableListOf<MovieEntity>()

                while (cursor.moveToNext()) {
                    val title = normalizeMovieTitle(cursor.getString(titleColumn).orEmpty())
                    if (title.isBlank()) continue

                    movies += MovieEntity(
                        title = title,
                        createdAt = cursor.longAt(createdAtColumn),
                        genre = cursor.stringAt(genreColumn),
                        isSeries = cursor.intAt(seriesColumn) != 0,
                        season = cursor.intAt(seasonColumn),
                        episode = cursor.intAt(episodeColumn),
                        watched = cursor.intAt(watchedColumn) != 0,
                        collection = cursor.stringAt(collectionColumn)
                            .ifBlank { CollectionNames.MAIN }
                    )
                }

                movies
            }
        } finally {
            database.close()
        }
    }

    private fun android.database.Cursor.requiredColumn(name: String): Int {
        return getColumnIndex(name).takeIf { it >= 0 }
            ?: throw IllegalArgumentException("This is not a Clof movies.db file: missing $name.")
    }

    private fun android.database.Cursor.optionalColumn(vararg names: String): Int {
        return names.firstNotNullOfOrNull { name ->
            getColumnIndex(name).takeIf { it >= 0 }
        } ?: -1
    }

    private fun android.database.Cursor.stringAt(column: Int): String {
        return if (column >= 0 && !isNull(column)) getString(column).orEmpty() else ""
    }

    private fun android.database.Cursor.intAt(column: Int): Int {
        return if (column >= 0 && !isNull(column)) getInt(column) else 0
    }

    private fun android.database.Cursor.longAt(column: Int): Long {
        return if (column >= 0 && !isNull(column)) getLong(column) else 0L
    }
}

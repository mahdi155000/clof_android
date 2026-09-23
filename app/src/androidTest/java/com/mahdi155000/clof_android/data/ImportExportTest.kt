package com.mahdi155000.clof_android.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ImportExportTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: MovieRepository
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var genreRepository: GenreRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        repository = MovieRepository(database.movieDao())
        collectionRepository = CollectionRepository(database.collectionDao())
        genreRepository = GenreRepository(database.genreDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun import_readsMoviesAndSkipsDuplicateTitles() = runBlocking {
        assertTrue(repository.insertMovie(MovieEntity(title = "Existing")))
        val source = File(context.cacheDir, "import-test.db")
        createSourceDatabase(source)

        val result = ClofDatabaseImporter(
            context, repository, collectionRepository, genreRepository
        ).importFrom(Uri.fromFile(source))

        assertEquals(1, result.imported)
        assertEquals(1, result.skipped)
        assertEquals("Imported", database.movieDao().getMovieByTitle("Imported")?.title)
        assertEquals("Archive", database.movieDao().getMovieByTitle("Imported")?.collection)
        source.delete()
    }

    @Test
    fun export_writesTheDatabaseFileToRequestedUri() = runBlocking {
        val appDatabase = AppDatabase.getDatabase(context)
        appDatabase.movieDao().insertMovieIfMissing(MovieEntity(title = "Exported"))
        val output = File(context.cacheDir, "export-test.db")

        ClofDatabaseExporter(context, appDatabase).exportTo(Uri.fromFile(output))

        assertTrue(output.exists())
        assertTrue(output.length() > 0)
        output.delete()
    }

    private fun createSourceDatabase(file: File) {
        file.delete()
        val source = SQLiteDatabase.openOrCreateDatabase(file, null)
        source.execSQL(
            """
            CREATE TABLE movies (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                genre TEXT,
                is_series INTEGER,
                season INTEGER,
                episode INTEGER,
                watched INTEGER,
                collection TEXT,
                notes TEXT,
                pinned INTEGER,
                custom_order INTEGER,
                personal_rating INTEGER,
                favorite INTEGER
            )
            """.trimIndent()
        )
        source.execSQL(
            "INSERT INTO movies VALUES (1, 'Existing', 'Drama', 0, 0, 0, 0, 'main', NULL, 0, 0, NULL, 0)"
        )
        source.execSQL(
            "INSERT INTO movies VALUES (2, 'Imported', 'Action', 1, 2, 3, 1, 'Archive', 'Note', 1, 4, 5, 1)"
        )
        source.close()
    }
}

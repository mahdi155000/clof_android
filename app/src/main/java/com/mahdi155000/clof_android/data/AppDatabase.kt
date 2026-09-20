package com.mahdi155000.clof_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MovieEntity::class, CollectionEntity::class, GenreEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun collectionDao(): CollectionDao
    abstract fun genreDao(): GenreDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clof.db"
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            insertDefaultCollections(db)
                            insertDefaultGenres(db)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            insertDefaultCollections(db)
                            insertDefaultGenres(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun insertDefaultCollections(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT OR IGNORE INTO collections(name) VALUES ('main')")
            db.execSQL("INSERT OR IGNORE INTO collections(name) VALUES ('watched')")
        }

        private fun insertDefaultGenres(db: SupportSQLiteDatabase) {
            defaultGenres.forEach { genre ->
                db.execSQL(
                    "INSERT OR IGNORE INTO genres(name) VALUES (?)",
                    arrayOf(genre)
                )
            }
        }

        val defaultGenres = listOf(
            "Action",
            "Adventure",
            "Animation",
            "Biography",
            "Comedy",
            "Crime",
            "Documentary",
            "Drama",
            "Family",
            "Fantasy",
            "Horror",
            "History",
            "Music",
            "Mystery",
            "Romance",
            "Science Fiction",
            "Sport",
            "Thriller",
            "War",
            "Western"
        )

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS collections " +
                        "(name TEXT NOT NULL, PRIMARY KEY(name))"
                )
                db.execSQL("INSERT OR IGNORE INTO collections(name) VALUES ('main')")
                db.execSQL(
                    "INSERT OR IGNORE INTO collections(name) " +
                        "SELECT DISTINCT CASE WHEN TRIM(collection) = '' THEN 'main' " +
                        "ELSE collection END FROM movies"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT OR IGNORE INTO collections(name) VALUES ('main')")
                db.execSQL("INSERT OR IGNORE INTO collections(name) VALUES ('watched')")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS genres " +
                        "(name TEXT NOT NULL, PRIMARY KEY(name))"
                )
                insertDefaultGenres(db)
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE movies ADD COLUMN inTrash INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
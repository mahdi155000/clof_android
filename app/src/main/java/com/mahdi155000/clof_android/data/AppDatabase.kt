package com.mahdi155000.clof_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MovieEntity::class, CollectionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun collectionDao(): CollectionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clof.db"
                ).addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL("INSERT OR IGNORE INTO collections(name) VALUES ('main')")
                        }
                    })
                    .build()

                INSTANCE = instance

                instance
            }
        }

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
    }
}

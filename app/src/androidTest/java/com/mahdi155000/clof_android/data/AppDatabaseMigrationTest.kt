package com.mahdi155000.clof_android.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun allMigrations_validateAgainstExportedSchemas() {
        for (startVersion in 1 until 10) {
            val databaseName = "migration-$startVersion-to-10"
            migrationTestHelper.createDatabase(databaseName, startVersion).close()
            migrationTestHelper.runMigrationsAndValidate(
                databaseName,
                10,
                true,
                *AppDatabase.ALL_MIGRATIONS
            )
        }
    }
}

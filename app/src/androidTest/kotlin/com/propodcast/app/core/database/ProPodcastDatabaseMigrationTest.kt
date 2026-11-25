package com.propodcast.app.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Version 1 (favorites only) → 2 (downloads) keeps the favorites. */
@RunWith(AndroidJUnit4::class)
class ProPodcastDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ProPodcastDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsFavorites_andAddsDownloads() {
        helper.createDatabase(DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO favorite_podcasts (id, title, publisher, image_url, thumbnail_url, description, " +
                    "total_episodes, listennotes_url, latest_pub_date_ms, added_at) " +
                    "VALUES ('p1', 'Podcast', 'Publisher', NULL, NULL, '', 3, NULL, NULL, 1)",
            )
        }

        helper.runMigrationsAndValidate(DB, 2, true).use { db ->
            db.query("SELECT title FROM favorite_podcasts").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Podcast", cursor.getString(0))
            }
            db.query("SELECT COUNT(*) FROM downloaded_episodes").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DB = "migration-test.db"
    }
}

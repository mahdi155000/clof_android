package com.mahdi155000.clof_android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieEditsTest {

    @Test
    fun positiveIntOrNull_rejectsZeroNegativeAndNonNumericValues() {
        assertEquals(4, " 4 ".toPositiveIntOrNull())
        assertEquals(null, "0".toPositiveIntOrNull())
        assertEquals(null, "-1".toPositiveIntOrNull())
        assertEquals(null, "invalid".toPositiveIntOrNull())
    }

    @Test
    fun withEdits_keepsIdentityAndWatchedStatus() {
        val movie = MovieEntity(
            id = 12,
            title = "Before",
            watched = true,
            collection = "Archive"
        )

        val edited = movie.withEdits(
            title = " After ",
            genre = " Drama ",
            collection = " ",
            isSeries = true,
            season = "2",
            episode = "3"
        )

        assertEquals(12, edited.id)
        assertTrue(edited.watched)
        assertEquals("After", edited.title)
        assertEquals("Drama", edited.genre)
        assertEquals("main", edited.collection)
        assertEquals(2, edited.season)
        assertEquals(3, edited.episode)
    }

    @Test
    fun withEdits_rejectsInvalidSeriesNumbers() {
        val movie = MovieEntity(title = "Movie")

        try {
            movie.withEdits(
                title = "Movie",
                genre = "",
                collection = "",
                isSeries = true,
                season = "0",
                episode = "not a number"
            )
            throw AssertionError("Expected invalid series numbers to be rejected")
        } catch (exception: IllegalArgumentException) {
            assertEquals("Season must be a positive integer.", exception.message)
        }
    }
}

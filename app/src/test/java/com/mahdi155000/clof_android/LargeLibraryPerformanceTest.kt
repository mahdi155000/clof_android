package com.mahdi155000.clof_android

import com.mahdi155000.clof_android.data.MovieEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class LargeLibraryPerformanceTest {

    private fun generateLargeLibrary(count: Int = 10_000): List<MovieEntity> {
        val genres = listOf("Action", "Comedy", "Drama", "Sci-Fi", "Horror")
        val collections = listOf("main", "favorites", "watchlist", "archive")

        return List(count) { index ->
            MovieEntity(
                id = index + 1,
                title = "Movie ${index + 1} - ${if (index % 2 == 0) "Matrix" else "Star Wars"}",
                createdAt = System.currentTimeMillis() - index * 1000L,
                genre = genres[index % genres.size],
                isSeries = index % 3 == 0,
                season = if (index % 3 == 0) (index % 5) + 1 else 0,
                episode = if (index % 3 == 0) (index % 20) + 1 else 0,
                watched = index % 2 == 0,
                collection = collections[index % collections.size],
                pinned = index % 100 == 0,
                customOrder = index,
                personalRating = if (index % 4 == 0) (index % 5) + 1 else null,
                favorite = index % 5 == 0
            )
        }
    }

    @Test
    fun filteringAndSorting_10000Items_completesFast() {
        val movies = generateLargeLibrary(10_000)

        val duration = measureTimeMillis {
            val result = filterAndSortMovies(
                movies = movies,
                searchText = "Matrix",
                typeFilter = "All",
                watchedFilter = "Watched",
                favoriteFilter = "All",
                ratingFilter = "All",
                selectedCollection = null,
                collectionFilter = "All",
                sortOption = "Recently Added"
            )
            assertTrue(result.isNotEmpty())
            assertTrue(result.all { it.title.contains("Matrix", ignoreCase = true) && it.watched })
        }

        println("Filtering and sorting 10,000 items took ${duration}ms")
        assertTrue("Filter & sort of 10,000 items should take less than 150ms", duration < 150)
    }

    @Test
    fun multiLevelSorting_10000Items_pinnedItemsAlwaysFirst() {
        val movies = generateLargeLibrary(10_000)

        val result = filterAndSortMovies(
            movies = movies,
            searchText = "",
            typeFilter = "All",
            watchedFilter = "All",
            favoriteFilter = "All",
            ratingFilter = "All",
            selectedCollection = null,
            collectionFilter = "All",
            sortOption = "Title A-Z"
        )

        assertEquals(10_000, result.size)
        val pinnedCount = movies.count { it.pinned }
        val topPinned = result.take(pinnedCount)
        assertTrue("Top items should all be pinned", topPinned.all { it.pinned })
    }
}

package com.mahdi155000.clof_android.data

fun String.toPositiveIntOrNull(): Int? =
    trim().toIntOrNull()?.takeIf { it > 0 }

/** Creates an edited movie while retaining state that is not editable in the form. */
fun MovieEntity.withEdits(
    title: String,
    genre: String,
    collection: String,
    isSeries: Boolean,
    season: String,
    episode: String,
    notes: String = "",
    personalRating: Int? = null,
    favorite: Boolean = false
): MovieEntity = copy(
    title = normalizeMovieTitle(title),
    genre = genre.trim(),
    collection = collection.trim().ifBlank { CollectionNames.MAIN },
    isSeries = isSeries,
    season = if (isSeries) {
        season.toPositiveIntOrNull()
            ?: throw IllegalArgumentException("Season must be a positive integer.")
    } else {
        0
    },
    episode = if (isSeries) {
        episode.toPositiveIntOrNull()
            ?: throw IllegalArgumentException("Episode must be a positive integer.")
    } else 0,
    notes = notes.trim().ifBlank { null },
    personalRating = personalRating?.coerceIn(1, 5),
    favorite = favorite
)

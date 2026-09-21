package com.mahdi155000.clof_android.data

/** Creates an edited movie while retaining state that is not editable in the form. */
fun MovieEntity.withEdits(
    title: String,
    genre: String,
    collection: String,
    isSeries: Boolean,
    season: String,
    episode: String
): MovieEntity = copy(
    title = title.trim(),
    genre = genre.trim(),
    collection = collection.trim().ifBlank { CollectionNames.MAIN },
    isSeries = isSeries,
    season = if (isSeries) season.toIntOrNull() ?: 1 else 0,
    episode = if (isSeries) episode.toIntOrNull() ?: 1 else 0
)

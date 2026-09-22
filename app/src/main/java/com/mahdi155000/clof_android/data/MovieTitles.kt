package com.mahdi155000.clof_android.data

import java.util.Locale

fun normalizeMovieTitle(title: String): String = title.trim()

fun movieTitleKey(title: String): String =
    normalizeMovieTitle(title).lowercase(Locale.ROOT)

fun deduplicateMoviesByTitle(movies: List<MovieEntity>): List<MovieEntity> =
    movies.distinctBy { movieTitleKey(it.title) }

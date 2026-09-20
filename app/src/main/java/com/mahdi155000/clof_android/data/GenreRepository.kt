package com.mahdi155000.clof_android.data

import kotlinx.coroutines.flow.Flow

class GenreRepository(
    private val genreDao: GenreDao
) {
    val genres: Flow<List<String>> = genreDao.observeGenres()

    suspend fun addGenre(name: String): Boolean {
        return genreDao.insertGenre(GenreEntity(name)) != -1L
    }

    suspend fun renameGenre(oldName: String, newName: String): Boolean {
        return genreDao.renameGenre(oldName, newName)
    }

    suspend fun removeGenre(name: String): Boolean {
        return genreDao.removeGenre(name)
    }

    suspend fun addMissingGenres(names: Collection<String>) {
        names.filter { it.isNotBlank() }.forEach { name ->
            genreDao.insertGenre(GenreEntity(name))
        }
    }
}

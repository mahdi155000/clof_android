package com.mahdi155000.clof_android.data

import kotlinx.coroutines.flow.Flow

class CollectionRepository(
    private val collectionDao: CollectionDao
) {
    val collections: Flow<List<String>> = collectionDao.observeCollections()

    suspend fun addCollection(name: String): Boolean {
        if (CollectionNames.isReserved(name)) return false
        return collectionDao.insertCollection(CollectionEntity(name)) != -1L
    }

    suspend fun renameCollection(oldName: String, newName: String): Boolean {
        return collectionDao.renameCollection(oldName, newName)
    }

    suspend fun removeCollection(name: String): Boolean {
        return collectionDao.removeCollection(name)
    }

    suspend fun addMissingCollections(names: Collection<String>) {
        names.forEach { name ->
            collectionDao.insertCollection(CollectionEntity(name))
        }
    }
}

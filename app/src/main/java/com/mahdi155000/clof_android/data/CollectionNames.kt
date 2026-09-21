package com.mahdi155000.clof_android.data

object CollectionNames {
    const val MAIN = "main"
    const val WATCHED = "watched"

    val DEFAULT = listOf(MAIN, WATCHED)
    val RESERVED = DEFAULT.toSet()

    fun isReserved(name: String): Boolean = name in RESERVED
}

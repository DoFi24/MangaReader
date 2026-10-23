package com.nilemnod.mangareader.core.navigation

interface SearchableScreen {

    suspend fun search(query: String)

    suspend fun searchGenre(name: String) = search(name)
}

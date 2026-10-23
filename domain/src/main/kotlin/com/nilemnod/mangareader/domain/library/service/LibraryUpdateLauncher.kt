package com.nilemnod.mangareader.domain.library.service

import com.nilemnod.mangareader.domain.category.model.Category

interface LibraryUpdateLauncher {

    suspend fun startNow(category: Category? = null): Boolean

    fun schedule(interval: Int? = null)
}

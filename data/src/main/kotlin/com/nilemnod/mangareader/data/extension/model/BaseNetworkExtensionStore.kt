package com.nilemnod.mangareader.data.extension.model

import com.nilemnod.mangareader.domain.extension.model.ExtensionStore

interface BaseNetworkExtensionStore {
    fun toExtensionStore(indexUrl: String): ExtensionStore
}

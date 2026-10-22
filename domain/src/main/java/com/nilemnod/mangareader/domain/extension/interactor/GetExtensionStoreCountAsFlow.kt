package com.nilemnod.mangareader.domain.extension.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.domain.extension.repository.ExtensionStoreRepository

@Inject
class GetExtensionStoreCountAsFlow(
    private val repository: ExtensionStoreRepository,
) {
    operator fun invoke() = repository.getCountAsFlow()
}

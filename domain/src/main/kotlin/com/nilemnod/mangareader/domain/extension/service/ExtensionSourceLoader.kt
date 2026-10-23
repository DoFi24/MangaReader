package com.nilemnod.mangareader.domain.extension.service

import com.nilemnod.mangareader.domain.source.api.ContentSource

interface ExtensionSourceLoader {

    fun load(classLoader: ClassLoader, pkgName: String, sourceClasses: String): List<ContentSource>
}

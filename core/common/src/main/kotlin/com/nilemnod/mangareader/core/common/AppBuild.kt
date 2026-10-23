package com.nilemnod.mangareader.core.common

object AppBuild {

    var isRelease: Boolean = true
        private set

    fun init(isRelease: Boolean) {
        this.isRelease = isRelease
    }
}

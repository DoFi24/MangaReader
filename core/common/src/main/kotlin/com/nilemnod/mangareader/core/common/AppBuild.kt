package com.nilemnod.mangareader.core.common

object AppBuild {

    class Info(
        val applicationId: String,
        val versionName: String,
        val versionCode: Int,
        val buildType: String,
        val isDebug: Boolean,
        val commitSha: String,
        val commitCount: String,
        val buildTime: String,
        val updateRepository: String,
    )

    lateinit var info: Info
        private set

    val applicationId: String
        get() = info.applicationId

    val versionName: String
        get() = info.versionName

    val isDebug: Boolean
        get() = info.isDebug

    val isRelease: Boolean
        get() = !::info.isInitialized || info.buildType == "release"

    val isNightly: Boolean
        get() = info.buildType == "nightly"

    val isFoss: Boolean
        get() = info.buildType == "foss"

    val updaterEnabled: Boolean
        get() = info.updateRepository.isNotBlank()

    val releaseUrl: String
        get() {
            val tag = if (isNightly) "r${info.commitCount}" else "v${info.versionName}"
            return "https://github.com/${info.updateRepository}/releases/tag/$tag"
        }

    fun init(info: Info) {
        this.info = info
    }
}

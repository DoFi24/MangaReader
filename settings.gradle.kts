pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://www.jitpack.io")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
    }

    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "MangaReader"
include(":app")
include(":baseline-profile")
include(":compat:tachiyomi")
include(":core:android")
include(":core:archive")
include(":core:backup")
include(":core:common")
include(":core:download")
include(":core:manga-ui")
include(":core:metadata")
include(":core:metro")
include(":core:navigation")
include(":core:network")
include(":core:reader")
include(":core:storage")
include(":core:ui")
include(":data")
include(":domain")
include(":feature:browse")
include(":feature:categories")
include(":feature:downloads")
include(":feature:extensions")
include(":feature:history")
include(":feature:library")
include(":feature:manga")
include(":feature:reader")
include(":feature:settings")
include(":feature:stats")
include(":feature:updates")
include(":feature:webview")
include(":feature:widget")
include(":i18n")
include(":icons:custom-material-symbols")
include(":icons:material-symbols")
include(":icons:simple-icons")
include(":source:common")
include(":source:local")
include(":source:mangadex")
include(":source:pepper")

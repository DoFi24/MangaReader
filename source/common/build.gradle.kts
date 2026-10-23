plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)
}

android {
    namespace = "com.nilemnod.mangareader.source.common"
}

dependencies {
    api(projects.domain)
    api(projects.core.network)
    implementation(projects.core.common)

    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
}

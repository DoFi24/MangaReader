plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.source.pepper"
}

dependencies {
    implementation(projects.source.common)
    implementation(projects.core.common)

    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.metro.runtime)
}

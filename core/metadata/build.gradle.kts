plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nilemnod.mangareader.core.metadata"
}

dependencies {
    implementation(projects.domain)

    implementation(libs.bundles.serialization)
}

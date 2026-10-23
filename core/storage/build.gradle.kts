plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.storage"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.metro)
    implementation(projects.domain)

    implementation(libs.bundles.kotlinx.coroutines)

    implementation(libs.metro.runtime)
}

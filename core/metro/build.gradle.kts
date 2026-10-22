plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.metro"
}

dependencies {
    implementation(libs.metro.runtime)
}

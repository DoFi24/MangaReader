plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nilemnod.mangareader.core.archive"
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.archive)
    implementation(libs.unifile)
}

plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.storage"
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.common)
    implementation(projects.core.metro)
    implementation(projects.domain)
    implementation(projects.i18n)
    implementation(projects.source.local)

    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.logcat)
    implementation(libs.okio)
    implementation(libs.unifile)

    implementation(libs.metro.runtime)
}

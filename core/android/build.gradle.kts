plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)
}

android {
    namespace = "com.nilemnod.mangareader.core.android"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.i18n)

    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core)
    implementation(libs.androidx.work)
    implementation(libs.material)
    implementation(libs.coil.core)
}

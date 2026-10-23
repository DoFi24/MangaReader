plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.compose)
    alias(libs.plugins.mangareader.spotless)
}

android {
    namespace = "com.nilemnod.mangareader.core.mangaui"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.domain)
    implementation(projects.extensionApi)
    implementation(projects.i18n)
    implementation(projects.icons.materialSymbols)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.uiUtil)
    implementation(libs.bundles.coil)
}

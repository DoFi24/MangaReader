plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.compose)
    alias(libs.plugins.mangareader.spotless)
}

android {
    namespace = "com.nilemnod.mangareader.core.mangaui"
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.domain)
    implementation(projects.i18n)
    implementation(projects.icons.customMaterialSymbols)
    implementation(projects.icons.materialSymbols)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.uiUtil)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.markdown)
}

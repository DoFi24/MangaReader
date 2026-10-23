plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.compose)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.reader"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.domain)
    implementation(projects.i18n)
    implementation(projects.icons.customMaterialSymbols)
    implementation(projects.icons.materialSymbols)

    implementation(libs.androidx.compose.ui)

    implementation(libs.metro.runtime)
}

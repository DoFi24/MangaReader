plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.compose)

    alias(libs.plugins.mangareader.spotless)
    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.presentation.widget"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.metro)
    implementation(projects.domain)
    implementation(projects.core.ui)
    implementation(projects.i18n)

    implementation(libs.androidx.glance.appWidget)
    implementation(libs.material)

    implementation(libs.kotlinx.datetime)

    implementation(libs.coil.core)

    implementation(libs.metro.runtime)
}

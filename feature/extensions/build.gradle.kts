plugins {
    alias(libs.plugins.mangareader.feature)
    alias(libs.plugins.metro)
}

android {
    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.common)
    implementation(projects.core.mangaUi)
    implementation(projects.core.metro)
    implementation(projects.core.navigation)
    implementation(projects.core.network)
    implementation(projects.domain)
    implementation(projects.icons.simpleIcons)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.shizuku)
    implementation(libs.bundles.voyager)
    implementation(libs.unifile)

    implementation(libs.metro.runtime)
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.viewmodel.compose)
}

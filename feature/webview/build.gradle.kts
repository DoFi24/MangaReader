plugins {
    alias(libs.plugins.mangareader.feature)
    alias(libs.plugins.metro)
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.common)
    implementation(projects.core.metro)
    implementation(projects.core.network)
    implementation(projects.domain)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.core)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.voyager)
    implementation(libs.composeWebview)

    implementation(libs.metro.runtime)
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.viewmodel.compose)
}

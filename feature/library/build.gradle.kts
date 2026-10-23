plugins {
    alias(libs.plugins.mangareader.feature)
    alias(libs.plugins.metro)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.download)
    implementation(projects.core.mangaUi)
    implementation(projects.core.metro)
    implementation(projects.core.navigation)
    implementation(projects.core.storage)
    implementation(projects.domain)
    implementation(projects.extensionApi)
    implementation(projects.source.local)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animationGraphics)
    implementation(libs.androidx.compose.uiUtil)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.voyager)
    implementation(libs.kotlinx.datetime)

    implementation(libs.metro.runtime)
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.viewmodel.compose)
}

plugins {
    alias(libs.plugins.mangareader.feature)
    alias(libs.plugins.metro)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.metro)
    implementation(projects.domain)
    implementation(projects.extensionApi)
    implementation(projects.source.local)

    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.voyager)

    implementation(libs.metro.runtime)
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.viewmodel.compose)
}

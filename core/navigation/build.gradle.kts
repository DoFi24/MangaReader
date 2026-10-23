plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.compose)
    alias(libs.plugins.mangareader.spotless)
}

android {
    namespace = "com.nilemnod.mangareader.core.navigation"
}

dependencies {
    api(projects.domain)
    api(libs.voyager.navigator)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.runtime)
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

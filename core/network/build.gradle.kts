plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.metro)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nilemnod.mangareader.core.network"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

dependencies {
    api(projects.core.common)
    implementation(projects.core.metro)
    implementation(projects.i18n)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.webkit)
    implementation(libs.quickJs)
    implementation(libs.metro.runtime)
}

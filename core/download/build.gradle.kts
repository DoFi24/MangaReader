plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.download"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
        )
    }
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.archive)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.metadata)
    implementation(projects.core.metro)
    implementation(projects.domain)
    implementation(projects.extensionApi)
    implementation(projects.i18n)

    implementation(libs.androidx.core)
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.diskLruCache)
    implementation(libs.injekt)
    implementation(libs.kotlinx.datetime)
    implementation(libs.unifile)

    implementation(libs.metro.runtime)
}

plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.backup"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.common)
    implementation(projects.core.download)
    implementation(projects.core.metro)
    implementation(projects.domain)
    implementation(projects.i18n)

    implementation(libs.androidx.core)
    implementation(libs.androidx.work)
    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.logcat)
    implementation(libs.okio)
    implementation(libs.unifile)

    implementation(libs.metro.runtime)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

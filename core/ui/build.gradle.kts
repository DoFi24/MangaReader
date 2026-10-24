plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.compose)

    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.core.ui"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
        )
    }
}

dependencies {
    api(projects.core.common)
    api(projects.i18n)
    implementation(projects.core.android)
    implementation(projects.core.metro)
    implementation(projects.icons.materialSymbols)
    implementation(projects.icons.customMaterialSymbols)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3NavSuite)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animationGraphics)
    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)
    implementation(libs.androidx.compose.uiUtil)

    implementation(libs.androidx.appCompat)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.voyager)
    implementation(libs.composeMaterialMotion)
    implementation(libs.materialKolor)
    implementation(libs.kotlinx.datetime)
    implementation(libs.metro.runtime)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
}

plugins {
    alias(libs.plugins.mangareader.feature)
    alias(libs.plugins.metro)
}

android {
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
        )
    }
}

dependencies {
    implementation(projects.core.android)
    implementation(projects.core.archive)
    implementation(projects.core.common)
    implementation(projects.core.download)
    implementation(projects.core.mangaUi)
    implementation(projects.core.metro)
    implementation(projects.core.navigation)
    implementation(projects.core.reader)
    implementation(projects.core.storage)
    implementation(projects.domain)
    implementation(projects.icons.customMaterialSymbols)
    implementation(projects.source.local)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.uiUtil)
    implementation(libs.androidx.core)
    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.viewPager)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.kotlinx.coroutines)
    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    implementation(libs.bundles.coil)
    implementation(libs.directionalViewPager) {
        exclude(group = "androidx.viewpager", module = "viewpager")
    }
    implementation(libs.image.decoder)
    implementation(libs.injekt)
    implementation(libs.logcat)
    implementation(libs.material)
    implementation(libs.okio)
    implementation(libs.photoView)
    implementation(libs.reorderable)
    implementation(libs.subsamplingScaleImageView) {
        exclude(module = "image-decoder")
    }
    implementation(libs.unifile)
    implementation(libs.webgpuviewer)

    implementation(libs.metro.runtime)
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.viewmodel.compose)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

import mangareader.gradle.tasks.GenerateLocalesConfigTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.mangareader.kotlin.multiplatform)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.moko.resources)
}

kotlin {
    android {
        namespace = "com.nilemnod.mangareader.i18n"

        // TODO(antsy): Remove when https://youtrack.jetbrains.com/issue/KT-83319 is resolved
        withHostTest { }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(libs.moko.resources)
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

androidComponents {
    onVariants { variant ->
        val resSource = variant.sources.res ?: return@onVariants

        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val task = tasks.register<GenerateLocalesConfigTask>("generate${variantName}LocalesConfig") {
            localeFiles.from(
                layout.projectDirectory.dir("src/commonMain/moko-resources").asFileTree.matching {
                    include("**/strings.xml", "**/plurals.xml")
                },
            )
        }
        resSource.addGeneratedSourceDirectory(task) { it.outputDir }
    }
}

multiplatformResources {
    resourcesPackage.set("com.nilemnod.mangareader.i18n")
}

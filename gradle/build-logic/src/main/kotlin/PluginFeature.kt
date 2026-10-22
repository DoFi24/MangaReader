import mangareader.gradle.extensions.alias
import mangareader.gradle.extensions.android
import mangareader.gradle.extensions.implementation
import mangareader.gradle.extensions.libs
import mangareader.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("UNUSED")
class PluginFeature : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.mangareader.android.library)
            alias(libs.plugins.mangareader.compose)
            alias(libs.plugins.mangareader.spotless)
        }

        android {
            namespace = "com.nilemnod.mangareader.feature.${project.name}"
        }

        dependencies {
            implementation(target.project(":core:ui"))
            implementation(target.project(":i18n"))

            implementation(libs.androidx.compose.foundation)
            implementation(libs.androidx.compose.material3)
        }
    }
}

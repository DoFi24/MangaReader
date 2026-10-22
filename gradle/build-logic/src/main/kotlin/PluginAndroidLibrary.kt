import mangareader.gradle.extensions.alias
import mangareader.gradle.extensions.libs
import mangareader.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UNUSED")
class PluginAndroidLibrary : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.android.library)
            alias(libs.plugins.mangareader.android.base)
        }
    }
}

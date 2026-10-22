import mangareader.gradle.extensions.alias
import mangareader.gradle.extensions.libs
import mangareader.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UNUSED")
class PluginAndroidTest : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.android.test)
            alias(libs.plugins.mangareader.android.base)
        }
    }
}

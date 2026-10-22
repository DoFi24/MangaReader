import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.CompileOptions
import com.android.build.api.dsl.DefaultConfig
import mangareader.gradle.configurations.configureKotlin
import mangareader.gradle.extensions.android
import mangareader.gradle.extensions.configureTest
import mangareader.gradle.extensions.coreLibraryDesugaring
import mangareader.gradle.extensions.libs
import mangareader.gradle.extensions.release
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

@Suppress("UNUSED")
class PluginAndroidBase : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        configureKotlin()
        configureTest()

        android {
            defaultConfig {
                minSdk = libs.versions.mangareader.android.sdk.min.get().toInt()
                if (this is ApplicationDefaultConfig) {
                    targetSdk = libs.versions.mangareader.android.sdk.target.get().toInt()
                }

                ndkVersion = libs.versions.mangareader.android.ndk.get()
            }

            compileSdk {
                version = release(libs.versions.mangareader.android.sdk.compile)
            }

            compileOptions {
                isCoreLibraryDesugaringEnabled = true
            }
        }

        dependencies {
            coreLibraryDesugaring(libs.android.desugar)
        }
    }
}

private fun CommonExtension.defaultConfig(block: DefaultConfig.() -> Unit) {
    defaultConfig.apply(block)
}

private fun CommonExtension.compileOptions(block: CompileOptions.() -> Unit) {
    compileOptions.apply(block)
}

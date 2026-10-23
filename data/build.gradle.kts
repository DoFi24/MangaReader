plugins {
    alias(libs.plugins.mangareader.android.library)
    alias(libs.plugins.mangareader.spotless)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.metro)
}

android {
    namespace = "com.nilemnod.mangareader.data"

    sqldelight {
        databases {
            create("Database") {
                packageName.set("com.nilemnod.mangareader.data")
                dialect(libs.sqldelight.sqliteDialect338)
                schemaOutputDirectory.set(project.file("./src/main/sqldelight"))
                generateAsync.set(true)
            }
        }
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

dependencies {
    implementation(projects.extensionApi)
    implementation(projects.domain)
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.metro)

    implementation(libs.metro.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.jsonOkio)
    implementation(libs.kotlinx.serialization.protobuf)

    implementation(libs.injekt)

    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.bundles.sqldelight)
}


@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    id("module.android")
    id("module.multiplatform")
    id("ktorwasm.workaround")
    alias(libs.plugins.serialization)
}

kotlin {

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":compottie"))
            implementation(project(":compottie-dot"))
            implementation(project(":compottie-network"))
            implementation(project(":compottie-resources"))
//            implementation("io.github.alexzhirkevich:compottie:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-dot:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-network:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-beta01")

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.serialization)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
}

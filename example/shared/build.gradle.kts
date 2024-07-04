plugins {
    id("module.android")
    id("module.multiplatform")
    id("ktorwasm.workaround")
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

            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.components.resources)
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

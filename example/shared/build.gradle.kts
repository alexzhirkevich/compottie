
@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    id("module.android")
    id("module.multiplatform")
    alias(libs.plugins.serialization)
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
            implementation(project(":compottie"))
            implementation(project(":compottie-dot"))
            implementation(project(":compottie-network"))
            implementation(project(":compottie-resources"))
//            implementation("io.github.alexzhirkevich:compottie:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-dot:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-network:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-beta01")

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0-beta01")
            implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.1.0")
            implementation("org.jetbrains.compose.material3.adaptive:adaptive-layout:1.1.0")
            implementation("org.jetbrains.compose.material3.adaptive:adaptive-navigation:1.1.0")

            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.serialization)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
//        desktopTest.dependencies {
//            implementation(compose.desktop.currentOs)
//        }
//        androidMain.dependencies {
//            implementation(libs.ktor.client.okhttp)
//        }
//
//        iosMain.dependencies {
//            implementation(libs.ktor.client.ios)
//        }
//
//        val desktopMain by getting {
//            dependencies {
//                implementation(libs.ktor.client.okhttp)
//            }
//        }
//
//        jsMain.dependencies {
//            implementation(libs.ktor.client.js)
//        }
//        wasmJsMain.dependencies {
//            implementation(libs.ktor.client.js)
//        }
    }
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
}

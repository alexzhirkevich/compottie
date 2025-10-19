@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

val _jvmTarget = findProperty("jvmTarget") as String


kotlin {

    applyDefaultHierarchyTemplate {
        common {
            group("jvmNative") {
                withAndroidTarget()
                withJvm()
                withIos()
                withMacos()
            }
            group("java"){
                withJvm()
                withAndroidTarget()
            }
            group("web") {
                withJs()
                withWasmJs()
            }
            group("skiko") {
                withJvm()
                withIos()
                withMacos()
                withJs()
                withWasmJs()
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
        }
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
        }
    }
    macosX64()
    macosArm64()
    jvm()

    js(IR) {
        browser()
    }

    wasmJs() {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
            implementation(project(":compottie"))
            implementation(project(":compottie-dot"))
            implementation(project(":compottie-network"))
            implementation(project(":compottie-resources"))

            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
//            implementation("io.github.alexzhirkevich:compottie:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-dot:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-network:2.0.0-beta01")
//            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-beta01")

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.5")
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
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
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

val jvmTarget = findProperty("jvmTarget") as String

android {
    namespace = "$group.${name.filter { it.isLetter() }}"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmTarget)
        targetCompatibility = JavaVersion.toVersion(jvmTarget)
    }
}

@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

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
            group("skiko") {
                withJvm()
                withIos()
                withMacos()
                withJs()
                withWasmJs()
            }
        }
    }

    android {
        //noinspection WrongGradleMethod
        namespace = "$group.${name.filter { it.isLetter() }}"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        buildToolsVersion = findProperty("android.buildToolsVersion") as String
        minSdk = (findProperty("android.minSdk") as String).toInt()
        androidResources.enable = true
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(findProperty("jvmTarget") as String)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
        }
    }
    macosArm64()
    jvm()

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")

            implementation(project(":compottie"))
            implementation(project(":compottie-dot"))
            implementation(project(":compottie-network"))
            implementation(project(":compottie-resources"))

//            implementation("io.github.alexzhirkevich:compottie:2.0.2")
//            implementation("io.github.alexzhirkevich:compottie-dot:2.0.2")
//            implementation("io.github.alexzhirkevich:compottie-network:2.0.2")
//            implementation("io.github.alexzhirkevich:compottie-resources:2.0.2")

            implementation(libs.androidx.lifecycle.viewmodel)


            implementation(libs.compose.material3)
            implementation(libs.compose.resources)
            implementation(libs.serialization)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.atomicfu)
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
            implementation(libs.coroutines.swing)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

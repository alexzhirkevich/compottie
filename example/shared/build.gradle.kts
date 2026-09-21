@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
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

val _jvmTarget = findProperty("jvmTarget") as String

// Computed outside of the `android {}` block: inside it `name` would refer to the Kotlin target name.
val androidNamespace = "$group.${name.filter { it.isLetter() }}"


kotlin {

    // withAndroidTarget() doesn't match the target of com.android.kotlin.multiplatform.library (KT-80409)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmNative") {
                withCompilations { it is KotlinMultiplatformAndroidCompilation }
                withJvm()
                withIos()
                withMacos()
            }
            group("java"){
                withJvm()
                withCompilations { it is KotlinMultiplatformAndroidCompilation }
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
        namespace = androidNamespace
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
        minSdk = (findProperty("android.minSdk") as String).toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
        }

        // Required for Compose Multiplatform resources (composeResources) on Android
        androidResources {
            enable = true
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

    js(IR) {
        browser()
    }

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

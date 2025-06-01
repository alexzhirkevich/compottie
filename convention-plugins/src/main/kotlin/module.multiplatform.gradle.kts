
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
}

val _jvmTarget = findProperty("jvmTarget")!! as String

kotlin {
    explicitApi()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
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

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
        }
    }


    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
        }
        publishLibraryVariants("release")
    }
//
//    iosArm64()
//    iosX64()
//    iosSimulatorArm64()
//    macosX64()
//    macosArm64()
//
//    js(IR) {
//        browser()
//    }
//
//    wasmJs() {
//        browser()
//    }
}
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    alias(libs.plugins.composeCompiler)

}

val _jvmTarget = findProperty("jvmTarget") as String

//@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
//    targetHierarchy.default()

    applyDefaultHierarchyTemplate()
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = _jvmTarget
            }
        }
    }
    androidTarget() {
        compilations.all {
            kotlinOptions {
                jvmTarget = _jvmTarget
            }
        }
    }
    js(IR) {
        browser()
    }
    wasmJs(){
        browser()
    }

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
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.foundation)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
        }
    }
}

android {
    namespace = "io.github.alexzhirkevich.compottie.example"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(_jvmTarget)
        targetCompatibility = JavaVersion.toVersion(_jvmTarget)
    }

    defaultConfig {
        minSdk = 24
    }
}
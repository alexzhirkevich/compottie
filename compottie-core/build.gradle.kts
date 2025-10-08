@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.compose.ExperimentalComposeLibrary


plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(libs.serialization)
            implementation(libs.okio)
            implementation(libs.atomicfu)
            api(libs.keight.core)
            implementation(libs.androidx.collection)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

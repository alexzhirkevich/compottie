@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.compose.ExperimentalComposeLibrary


plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.atomicfu)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation) {
                exclude("org.jetbrains.kotlinx", "atomicfu")
            }
            implementation(libs.serialization)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest) {
                exclude("org.jetbrains.kotlinx", "atomicfu")
            }
        }
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs) {
                exclude("org.jetbrains.kotlinx", "atomicfu")
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.startup)
        }
    }
}

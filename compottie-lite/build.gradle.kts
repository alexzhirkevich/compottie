@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.compose.ExperimentalComposeLibrary


plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(libs.compose.foundation)
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

@file:Suppress("DSL_SCOPE_VIOLATION")


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
            implementation(libs.compose.ui.test)
        }
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

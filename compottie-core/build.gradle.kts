@file:Suppress("DSL_SCOPE_VIOLATION")


plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    explicitApi()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.serialization)
            implementation(libs.okio)
            implementation(libs.atomicfu)
            api(libs.keight.core)
            implementation(libs.androidx.collection)
        }
        skikoMain.dependencies {
            implementation(libs.skiko)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
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

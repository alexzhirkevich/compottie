@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.serialization)
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(libs.serialization)
            implementation(libs.okio)
            implementation(libs.coroutines.core)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

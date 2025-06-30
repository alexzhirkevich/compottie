@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.serialization)
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie"))
            implementation(compose.ui)
            implementation(libs.serialization)
            implementation(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

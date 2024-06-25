@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.serialization)
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.serialization)
            implementation(libs.okio)
            implementation(libs.okio.fakefilesystem)
            implementation(libs.coroutines.core)
            implementation(project(":compottie"))
        }
    }
}


plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(project(":compottie-dot"))
            implementation(libs.serialization)
            implementation(libs.atomicfu)
            implementation(libs.compose.ui)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

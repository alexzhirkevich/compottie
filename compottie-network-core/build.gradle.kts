
plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(project(":compottie-dot"))
            implementation(libs.serialization)
            implementation(libs.atomicfu)
            implementation(compose.ui)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

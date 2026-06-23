plugins {
    alias(libs.plugins.serialization)
}

kotlin {
    explicitApi()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(libs.serialization)
            implementation(libs.okio)
            implementation(libs.coroutines.core)
            implementation(libs.compose.runtime)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

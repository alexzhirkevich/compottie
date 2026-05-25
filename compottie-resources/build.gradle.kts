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
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.coroutines.core)
        }
    }
}

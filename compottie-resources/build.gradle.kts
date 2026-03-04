plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}
kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(libs.compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
        }
    }
}

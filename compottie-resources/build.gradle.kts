
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
        }
    }
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
            api(project(":compottie"))
        }
    }
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(project(":compottie-dot"))
            implementation(compose.ui)
            implementation(libs.serialization)
            implementation(libs.atomicfu)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

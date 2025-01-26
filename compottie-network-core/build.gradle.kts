
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie"))
            implementation(project(":compottie-dot"))
            implementation(compose.ui)
            implementation(libs.serialization)
            compileOnly(libs.atomicfu)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

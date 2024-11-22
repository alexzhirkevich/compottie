
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie"))
            implementation(compose.ui) {
                exclude("org.jetbrains.kotlinx", "atomicfu")
            }
            implementation(compose.components.resources) {
                exclude("org.jetbrains.kotlinx", "atomicfu")
            }
            implementation(libs.coroutines.core)
        }
    }
}

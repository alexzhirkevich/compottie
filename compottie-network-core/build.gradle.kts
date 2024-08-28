plugins {
    id("kotlinx-atomicfu")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie"))
            implementation(project(":compottie-dot"))
            implementation(compose.ui)
            implementation(libs.serialization)
            api(libs.okio)
            implementation(libs.coroutines.core)
        }
    }
}

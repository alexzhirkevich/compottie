plugins {
    id("kotlinx-atomicfu")
    id("ktorwasm.workaround")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.serialization)
            api(libs.okio)
            implementation(libs.coroutines.core)
            api(project(":compottie"))
            implementation(project(":compottie-dot"))
        }
    }
}

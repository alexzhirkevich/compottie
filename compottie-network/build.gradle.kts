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
            api(libs.ktor.client.core)
            api(project(":compottie"))
            implementation(project(":compottie-dot"))
        }
    }
}

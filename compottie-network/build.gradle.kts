plugins {
    id("kotlinx-atomicfu")
    id("ktorwasm.workaround")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie"))
            api(project(":compottie-network-core"))
            implementation(project(":compottie-dot"))
            implementation(compose.ui)
            implementation(libs.serialization)
            api(libs.okio)
            api(libs.ktor.client.core)
        }
    }
}


kotlin {
    explicitApi()

    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            api(project(":compottie-network-core"))
            implementation(project(":compottie-dot"))
            implementation(libs.compose.runtime)
            implementation(libs.serialization)
            api(libs.okio)
            api(libs.ktor.client.core)
        }
    }
}

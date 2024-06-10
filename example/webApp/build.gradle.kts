plugins {
    id("org.jetbrains.compose")
    kotlin("multiplatform")
    alias(libs.plugins.composeCompiler)

}

kotlin {
    js(IR){
        browser()
        binaries.executable()
    }

    wasmJs(){
        browser()
        binaries.executable()
    }
    sourceSets {

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(project(":example:shared"))
        }
    }
}

configurations
    .filter { it.name.contains("wasmJs") }
    .onEach {
        it.resolutionStrategy.eachDependency {
            if (requested.group.startsWith("io.ktor") &&
                requested.name.startsWith("ktor-client-")
            ) {
                useVersion("3.0.0-wasm2")
            }
        }
    }


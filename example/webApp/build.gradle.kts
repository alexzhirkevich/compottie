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


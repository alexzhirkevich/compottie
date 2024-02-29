plugins {
    id("org.jetbrains.compose")
    kotlin("multiplatform")
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

compose.experimental.web.application{}

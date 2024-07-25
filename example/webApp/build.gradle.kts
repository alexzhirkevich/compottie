plugins {
    kotlin("multiplatform")
}

kotlin {

    js(IR) {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(project(":example:shared"))
        }

        val webMain by creating {
            jsMain.get().dependsOn(this)
            wasmJsMain.get().dependsOn(this)
        }
    }
}

plugins.apply("ktorwasm.workaround")


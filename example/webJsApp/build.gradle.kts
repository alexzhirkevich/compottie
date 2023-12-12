plugins {
    id("org.jetbrains.compose")
    kotlin("multiplatform")
}

kotlin {
    js(IR){
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(project(":example:shared"))
            }
        }
    }
}

compose.experimental.web.application{}

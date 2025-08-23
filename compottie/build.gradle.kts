
plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":compottie-core"))
            implementation(compose.foundation)
            implementation(libs.keight)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

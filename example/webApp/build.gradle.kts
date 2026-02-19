@file:Suppress("DSL_SCOPE_VIOLATION")


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}


kotlin {

    applyDefaultHierarchyTemplate()

    js {
        browser()

        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        webMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(project(":example:shared"))
        }
    }
}


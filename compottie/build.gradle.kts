@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.serialization)
    id("kotlinx-atomicfu")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(libs.serialization)
        }

        androidMain.dependencies {
            implementation(libs.androidx.startup)
        }
    }
}

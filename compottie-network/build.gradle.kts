    @file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Base64

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    id("kotlinx-atomicfu")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.serialization)
            api(libs.okio)
            api(libs.ktor.client.core)
            implementation(project(":compottie"))
            implementation(project(":compottie-dot"))
        }
    }
}

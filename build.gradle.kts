@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {

    id("root.publication")
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.serialization).apply(false)
}

buildscript {
    dependencies {
        classpath(libs.gp.atomicfu)
        classpath(libs.nexus.publish)
    }
}

subprojects {

    plugins.apply("org.jetbrains.compose")
    plugins.apply("org.jetbrains.kotlin.plugin.compose")

    if (!name.startsWith("compottie")) {
        return@subprojects
    }

    plugins.apply("module.publication")
    plugins.apply("module.android")
    plugins.apply("module.multiplatform")
}


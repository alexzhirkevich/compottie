@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    id("root.publication")
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.serialization).apply(false)
    alias(libs.plugins.atomicfu).apply(false)
}

buildscript {
    dependencies {
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


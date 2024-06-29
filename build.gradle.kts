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
    id("maven-publish")
    id("signing")
}

buildscript {
    dependencies {
        classpath(libs.gp.atomicfu)
        classpath(libs.nexus.publish)
    }
}

val jvmTarget = findProperty("jvmTarget") as String

val _group = findProperty("group") as String

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

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

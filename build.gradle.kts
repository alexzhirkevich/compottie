@file:Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.serialization).apply(false)
}

buildscript {
    dependencies {
        classpath(libs.gp.atomicfu)

    }
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

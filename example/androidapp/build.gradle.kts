
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

val _jvmTarget = findProperty("jvmTarget") as String

android {
    namespace = "io.github.alexzhirkevich.compottie.example.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.alexzhirkevich.compottie.example.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(_jvmTarget)
        targetCompatibility = JavaVersion.toVersion(_jvmTarget)
    }
    kotlinOptions {
        jvmTarget = _jvmTarget
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildFeatures { compose = true }
}

dependencies {

    implementation(project(":example:shared"))
    implementation(project(":compottie"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0")
}
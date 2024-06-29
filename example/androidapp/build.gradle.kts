
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.android.application)
}

val _jvmTarget = findProperty("jvmTarget") as String

android {
    namespace = "$group.compottie.example.android"
    compileSdk = 34

    defaultConfig {
        applicationId = namespace
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = project.version.toString()

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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.compose)
    implementation(compose.uiTooling)
    implementation(compose.preview)
}
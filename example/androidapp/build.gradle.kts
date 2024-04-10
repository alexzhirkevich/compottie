plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.compose")
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
}

dependencies {
    implementation(project(":example:shared"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(libs.junit)
    implementation(libs.android.junit)
    implementation(libs.compose.ui.tooling.preview)
}
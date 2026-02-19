
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}


kotlin {
    jvmToolchain((findProperty("jvmTarget") as String).toInt())
}

android {
    namespace = "io.github.alexzhirkevich.compottie.example.android"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = namespace
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = project.version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(compose.uiTooling)
    implementation(compose.preview)
    implementation(libs.compose.foundation)
    implementation(compose.components.resources)
}
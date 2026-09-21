
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
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


    // AGP 9 built-in Kotlin derives the Kotlin jvmTarget from these
    compileOptions {
        val javaVersion = JavaVersion.toVersion(findProperty("jvmTarget") as String)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
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
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.resources)
}
import org.gradle.api.JavaVersion


plugins {
    id("com.android.library")
}

val jvmTarget = findProperty("jvmTarget") as String

android {
    namespace = "$group.${name.filter { it.isLetter() }}"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmTarget)
        targetCompatibility = JavaVersion.toVersion(jvmTarget)
    }
}

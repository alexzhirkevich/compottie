
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}


kotlin {

    jvm()
    
    sourceSets {
       jvmMain {
            kotlin.srcDirs("src/main/kotlin")
            dependencies {
                implementation(project(":example:shared"))
                implementation(project(":compottie"))

                implementation(libs.serialization)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "Main_desktopKt"
    }
}
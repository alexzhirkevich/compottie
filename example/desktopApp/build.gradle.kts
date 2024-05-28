
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.composeCompiler)
}


kotlin {
    jvm {
        withJava()
//        compilations.all {
//            kotlinOptions.jvmTarget = "11"
//        }
    }
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDirs("src/main/kotlin")
            dependencies {
                implementation(project(":example:shared"))
                implementation(project(":compottie"))

                implementation(libs.serialization)
                implementation(compose.desktop.currentOs)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                api(compose.materialIconsExtended)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "Main_desktopKt"
    }
}
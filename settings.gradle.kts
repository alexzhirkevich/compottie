pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

    }
}

rootProject.name = "compottie"
include(":compottie")
include(":example:desktopApp")
include(":example:webJsApp")
include(":example:androidapp")
include(":example:shared")

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
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

rootProject.name = "compottie"
include(":compottie")
include(":compottie-dot")
include(":compottie-network")
include(":compottie-resources")
include(":example:desktopApp")
include(":example:webApp")
include(":example:androidapp")
include(":example:shared")

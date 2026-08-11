pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://redirector.kotlinlang.org/maven/compose-dev")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://redirector.kotlinlang.org/maven/compose-dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven( "https://central.sonatype.com/repository/maven-snapshots")
//        mavenLocal()
    }
}

rootProject.name = "compottie"
include(":compottie-core")
include(":compottie")
include(":compottie-lite")
include(":compottie-dot")
include(":compottie-network")
include(":compottie-network-core")
include(":compottie-resources")
include(":example:desktopApp")
include(":example:webApp")
include(":example:androidApp")
include(":example:shared")


@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Base64

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
    id("maven-publish")
    id("signing")
}

group = "io.github.alexzhirkevich"
version = libs.versions.compottie.get()

val _jvmTarget = findProperty("jvmTarget") as String

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("skiko") {
                withJvm()
                withIos()
                withMacos()
                withJs()
                withWasmJs()
            }
        }
    }

    androidTarget{
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = _jvmTarget
            }
        }
    }


    iosArm64()
    iosX64()
    iosSimulatorArm64()

    wasmJs(){
        browser()
    }
    js(IR){
        browser()
    }
    jvm("desktop"){
        compilations.all {
            kotlinOptions {
                jvmTarget = _jvmTarget
            }
        }
    }

    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.coroutines.core)
            implementation(project(":compottie"))
        }
    }
}

android {
    namespace = "io.github.alexzhirkevich.compottie"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(_jvmTarget)
        targetCompatibility = JavaVersion.toVersion(_jvmTarget)
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}
// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

publishing {
    if (System.getenv("OSSRH_PASSWORD")!=null) {

        repositories {
            maven {
                val releasesRepoUrl =
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl =
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = if (version.toString().contains("dev")) {
                    uri(snapshotsRepoUrl)
                } else {
                    uri(releasesRepoUrl)
                }
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set("Compottie")
            description.set("Compose Multiplatform lottie animation")
            url.set("https://github.com/alexzhirkevich/compottie")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("alexzhirkevich")
                    name.set("Alexander Zhirkevich")
                    email.set("sasha.zhirkevich@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/alexzhirkevich/compottie")
                connection.set("scm:git:git://github.com/alexzhirkevich/compottie.git")
                developerConnection.set("scm:git:git://github.com/alexzhirkevich/compottie.git")
            }
        }
    }
}

if (System.getenv("GPG_KEY") != null) {
    signing {
        useInMemoryPgpKeys(
            Base64.getDecoder().decode(System.getenv("GPG_KEY")).decodeToString(),
            System.getenv("GPG_KEY_PWD"),
        )
        sign(publishing.publications)
    }
}
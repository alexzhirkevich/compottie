@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.serialization).apply(false)
    id("maven-publish")
    id("signing")
}

buildscript {
    dependencies {
        classpath(libs.gp.atomicfu)
    }
}

val jvmTarget = findProperty("jvmTarget") as String

val _group = findProperty("group") as String

subprojects {
    if (!name.startsWith("compottie")){
        return@subprojects
    }

    plugins.apply("maven-publish")
    plugins.apply("signing")
    plugins.apply("org.jetbrains.kotlin.multiplatform")
    plugins.apply("com.android.library")

    group = _group
    version = findProperty("version") as String

    kotlin {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        applyDefaultHierarchyTemplate {
            common {
                group("jvmNative") {
                    withAndroidTarget()
                    withJvm()
                    withIos()
                    withMacos()
                }
                group("web"){
                    withJs()
                    withWasmJs()
                }
                group("skiko"){
                    withJvm()
                    withIos()
                    withMacos()
                    withJs()
                    withWasmJs()
                }
            }
        }

        androidTarget {
            compilations.all {
                kotlinOptions.jvmTarget = jvmTarget
            }
            publishLibraryVariants("release")
        }

        iosArm64()
        iosX64()
        iosSimulatorArm64()
        macosX64()
        macosArm64()


        jvm("desktop") {
            compilations.all {
                kotlinOptions.jvmTarget = jvmTarget
            }
        }

        js(IR) {
            browser()
        }

        wasmJs() {
            browser()
        }
    }

    android {
        namespace = "$_group.${name.filter { it.isLetter() }}"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()

        defaultConfig {
            minSdk = (findProperty("android.minSdk") as String).toInt()
        }
        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(jvmTarget)
            targetCompatibility = JavaVersion.toVersion(jvmTarget)
        }
    }

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

    val signingTasks = tasks.withType<Sign>()

    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(signingTasks)
    }
    publishing {
        if (rootProject.ext.has("ossrhPassword")) {
            repositories.maven {
                val releasesRepoUrl =
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl =
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    uri(snapshotsRepoUrl)
                } else {
                    uri(releasesRepoUrl)
                }
                credentials {
                    username = rootProject.ext.get("ossrhUsername").toString()
                    password = rootProject.ext.get("ossrhPassword").toString()
                }
            }
        }

        publications.withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("Compottie")
                description.set("Compose Multiplatform Lottie animation renderer")
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
}

android {
    namespace = "$_group.${project.name}"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

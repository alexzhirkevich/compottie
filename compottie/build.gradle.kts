@file:Suppress("DSL_SCOPE_VIOLATION")

import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

group = "io.github.alexzhirkevich"
version = libs.versions.compottie.get()

val _jvmTarget = findProperty("jvmTarget") as String

kotlin {

    applyDefaultHierarchyTemplate()

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
            implementation(compose.foundation)
            implementation(compose.animation)
        }

        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.lottie.android)
        }

        val skikoMain by creating {
            dependsOn(commonMain.get())
            desktopMain.dependsOn(this)
            iosMain.get().dependsOn(this)
            macosMain.get().dependsOn(this)
            jsMain.get().dependsOn(this)
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

//ext["signing.keyId"] = null
//ext["signing.password"] = null
//ext["signing.secretKeyRingFile"] = null
//ext["ossrhUsername"] = null
//ext["ossrhPassword"] = null

//extra.apply {
//    val publishPropFile = rootProject.file("local.properties")
//    if (publishPropFile.exists()) {
//        Properties().apply {
//            load(publishPropFile.inputStream())
//        }.forEach { name, value ->
//            if (name == "signing.secretKeyRingFile") {
//                set(name.toString(), rootProject.file(value.toString()).absolutePath)
//            } else {
//                set(name.toString(), value)
//            }
//        }
//    } else {
//        ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
//        ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
//        ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
//        ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
//        ext["ossrhPassword"]= System.getenv("OSSRH_PASSWORD")
//    }
//}
//
//val javadocJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//}
//// https://github.com/gradle/gradle/issues/26091
//val signingTasks = tasks.withType<Sign>()
//tasks.withType<AbstractPublishToMaven>().configureEach {
//    dependsOn(signingTasks)
//}

//publishing {
//    if (rootProject.ext("local.properties").exists()) {
//
//        repositories {
//            maven {
//                val releasesRepoUrl =
//                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
//                val snapshotsRepoUrl =
//                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
//                url = if (version.toString().endsWith("SNAPSHOT")) {
//                    uri(snapshotsRepoUrl)
//                } else {
//                    uri(releasesRepoUrl)
//                }
//                credentials {
//                    username = project.ext.get("ossrhUsername").toString()
//                    password = project.ext.get("ossrhPassword").toString()
//                }
//            }
//        }
//    }
//
//    publications.withType<MavenPublication> {
//        artifact(javadocJar)
//        pom {
//            name.set("QRose")
//            description.set("Styled QR code generation library for Compose Multiplatform")
//            url.set("https://github.com/alexzhirkevich/qrose")
//
//            licenses {
//                license {
//                    name.set("MIT")
//                    url.set("https://opensource.org/licenses/MIT")
//                }
//            }
//            developers {
//                developer {
//                    id.set("alexzhirkevich")
//                    name.set("Alexander Zhirkevich")
//                    email.set("sasha.zhirkevich@gmail.com")
//                }
//            }
//            scm {
//                url.set("https://github.com/alexzhirkevich/qrose")
//                connection.set("scm:git:git://github.com/alexzhirkevich/qrose.git")
//                developerConnection.set("scm:git:git://github.com/alexzhirkevich/qrose.git")
//            }
//        }
//    }
//}
//
//signing {
//    sign(publishing.publications)
//}
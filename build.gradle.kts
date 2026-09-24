import com.android.build.api.dsl.KotlinMultiplatformAndroidCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.multiplatform.library).apply(false)
    alias(libs.plugins.mavenPublish)
}

rootProject.projectDir.resolve("local.properties").let {
    if (it.exists()) {
        Properties().apply {
            load(FileInputStream(it))
        }.forEach { (k,v)-> rootProject.ext.set(k.toString(), v) }
        System.getenv().forEach { (k,v) ->
            rootProject.ext.set(k, v)
        }
    }
}

kotlin {
    jvm()
    explicitApi()
}

val _jvmTarget = findProperty("jvmTarget").toString()

subprojects {
    group = findProperty("group") as String
    version = findProperty("version") as String

    if (!name.startsWith("compottie")) {
        return@subprojects
    }

    plugins.apply("org.jetbrains.kotlin.multiplatform")
    plugins.apply("com.vanniktech.maven.publish")
    plugins.apply("com.android.kotlin.multiplatform.library")

    androidLibrarySetup()
    multiplatformSetup()
    publicationSetup()
}

fun Project.publicationSetup() {
    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

//        coordinates(group.toString(), name, version.toString())

        pom {
            name.set("Compottie")
            description.set("Compose Multiplatform Lottie animation library")
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


@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.multiplatformSetup() {
    project.kotlin {

        // NOTE: withAndroidTarget() only matches the legacy KotlinAndroidTarget and silently
        // drops the target created by com.android.kotlin.multiplatform.library (KT-80409),
        // which would leave androidMain disconnected from jvmNativeMain / javaMain.
        // Match the new Android compilations explicitly instead.
        applyDefaultHierarchyTemplate {
            common {
                group("jvmNative") {
                    withCompilations { it is KotlinMultiplatformAndroidCompilation }
                    withJvm()
                    withNative()
                }
                group("java"){
                    withJvm()
                    withCompilations { it is KotlinMultiplatformAndroidCompilation }
                }
                group("skiko") {
                    withJvm()
                    withNative()
                    withJs()
                    withWasmJs()
                }
                group("desktopNative") {
                    withJvm()
                    withNative()
                }
                group("nonJvm"){
                    withNative()
                    withWasmJs()
                    withJs()
                }
            }
        }

        jvm("desktop") {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
        }


        iosArm64()
        iosSimulatorArm64()
        macosArm64()

        js(IR) {
            browser()
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }
    }
}


fun Project.androidLibrarySetup() {
    val androidNamespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
    val androidCompileSdk = (findProperty("android.compileSdk") as String).toInt()
    val androidMinSdk = (findProperty("android.minSdk") as String).toInt()

    // The android target is provided by com.android.kotlin.multiplatform.library
    // (replaces the top-level `android {}` / LibraryExtension and `androidTarget {}`).
    // Java compilation is disabled by default (no withJava()) - the libraries have no Java sources.
    // Only one variant is published by this plugin, so publishLibraryVariants() is gone.
    kotlin {
        targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
            namespace = androidNamespace
            compileSdk = androidCompileSdk
            minSdk = androidMinSdk
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
        }
    }
}

import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
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
    plugins.apply("android-library")

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

        applyDefaultHierarchyTemplate {
            common {
                group("jvmNative") {
                    withAndroidTarget()
                    withJvm()
                    withIos()
                    withMacos()
                }
                group("java"){
                    withJvm()
                    withAndroidTarget()
                }
                group("skiko") {
                    withJvm()
                    withIos()
                    withMacos()
                    withJs()
                    withWasmJs()
                }
                group("desktopNative") {
                    withJvm()
                    withIos()
                    withMacos()
                }
            }
        }

        jvm("desktop") {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
        }


        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(_jvmTarget))
            }
            publishLibraryVariants("release")
        }

        iosArm64()
        iosSimulatorArm64()
        iosX64()
        macosArm64()
        macosX64()

        js(IR) {
            browser()
        }

        wasmJs() {
            browser()
        }
    }
}


fun Project.androidLibrarySetup() {
    extensions.configure<LibraryExtension> {
        namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
        compileSdk = (findProperty("android.compileSdk") as String).toInt()

        defaultConfig {
            minSdk = (findProperty("android.minSdk") as String).toInt()
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}

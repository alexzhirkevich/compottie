import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.mavenPublish) apply false
}

rootProject.projectDir.resolve("local.properties").let {
    if (it.exists()) {
        Properties().apply {
            load(FileInputStream(it))
        }.forEach { (k, v) -> rootProject.ext.set(k.toString(), v) }
        System.getenv().forEach { (k, v) ->
            rootProject.ext.set(k, v)
        }
    }
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

    multiplatformSetup()
    publicationSetup()
}

fun Project.publicationSetup() {
    plugins.withType<MavenPublishPlugin> {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()

//          coordinates(group.toString(), name, version.toString())

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
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.multiplatformSetup() {
    plugins.withType<KotlinMultiplatformPluginWrapper> {
        extensions.configure<KotlinMultiplatformExtension> {

            applyDefaultHierarchyTemplate {
                common {
                    group("jvmNative") {
                        withAndroidTarget()
                        withJvm()
                        withIos()
                        withMacos()
                    }
                    group("java") {
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

            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
                compileSdk = (findProperty("android.compileSdk") as String).toInt()
                minSdk = (findProperty("android.minSdk") as String).toInt()
                compilerOptions {
                    jvmTarget = JvmTarget.fromTarget(_jvmTarget)
                }
            }

            jvm("desktop") {
                compilerOptions {
                    jvmTarget = JvmTarget.fromTarget(_jvmTarget)
                }
            }

            iosArm64()
            iosSimulatorArm64()
            macosArm64()

            js { browser() }

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
            }

            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xexpect-actual-classes"
                )
            }
        }
    }
}

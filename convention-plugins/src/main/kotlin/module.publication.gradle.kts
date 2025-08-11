import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

import java.util.Base64


plugins {
    `maven-publish`
    signing
}


//val javadocJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//}
//
//val signingTasks = tasks.withType<Sign>()
//tasks.withType<AbstractPublishToMaven>().configureEach {
//    dependsOn(signingTasks)
//}
val fixOptionString = "Set it in gradle.properties or GRADLE_USER_HOME/GRADLE_HOME"

val userName = providers.gradleProperty("gpr.user").orNull
    ?: throw GradleException("User name must be provided for gpr.user. $fixOptionString")

val token = providers.gradleProperty("gpr.token").orNull
    ?: throw GradleException("Token must be provided for gpr.token. $fixOptionString")


publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/orispok/compottie")
            credentials {
                username = userName
                password = token
            }
        }
    }
}

//publishing {
//    publications.withType<MavenPublication> {
//        artifact(javadocJar)
//        pom {
//            name.set("Compottie")
//            description.set("Compose Multiplatform lottie animation")
//            url.set("https://github.com/orispok/compottie")
//
//            licenses {
//                license {
//                    name.set("MIT")
//                    url.set("https://opensource.org/licenses/MIT")
//                }
//            }
//            developers {
//                developer {
//                    id.set("orispok")
//                    name.set("orispok")
//                    email.set("")
//                }
//            }
//            scm {
//                url.set("https://github.com/orispok/compottie")
//                connection.set("scm:git:git://github.com/orispok/compottie.git")
//                developerConnection.set("scm:git:git://github.com/orispok/compottie.git")
//            }
//        }
//    }
//}


//signing {
//    useInMemoryPgpKeys(
//        Base64.getDecoder().decode(
//            rootProject.ext.takeIf { it.has("GPG_KEY") }?.get("GPG_KEY") as? String ?: return@signing
//        ).decodeToString(),
//        rootProject.ext.takeIf { it.has("GPG_KEY_PWD") }?.get("GPG_KEY_PWD") as? String ?: return@signing
//    )
//    sign(publishing.publications)
//}
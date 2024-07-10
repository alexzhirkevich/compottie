import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

import java.util.Base64


plugins {
    `maven-publish`
    signing
}


val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

publishing {
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

if (System.getenv("GPG_KEY") != null || findProperty("GPG_KEY") != null) {
    signing {
        useInMemoryPgpKeys(
            Base64.getDecoder().decode(
                System.getenv("GPG_KEY") ?: findProperty("GPG_KEY") as String
            ).decodeToString(),
            System.getenv("GPG_KEY_PWD") ?: findProperty("GPG_KEY_PWD") as String,
        )
        sign(publishing.publications)
    }
}
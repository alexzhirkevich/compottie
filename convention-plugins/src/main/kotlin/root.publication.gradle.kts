import gradle.kotlin.dsl.accessors._2502cef48cff830615fe1c6d6ab5e104.ext
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

rootProject.projectDir.resolve("local.properties").let {
    if (it.exists()) {
        Properties().apply {
            load(FileInputStream(it))
        }.also {
            it.forEach { (k,v)-> rootProject.ext.set(k.toString(), v) }
        }
        System.getenv().forEach { (k,v) ->
            rootProject.ext.set(k, v)
        }
    }
}

allprojects {
    group = findProperty("group") as String
    version = findProperty("version") as String
}

nexusPublishing {
    // Configure maven central repository
    // https://github.com/gradle-nexus/publish-plugin#publishing-to-maven-central-via-sonatype-ossrh
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))


            username.set(rootProject.ext.takeIf { it.has("OSSRH_USERNAME") }?.get("OSSRH_USERNAME") as? String? ?: return@sonatype)
            password.set(rootProject.ext.takeIf { it.has("OSSRH_PASSWORD") }?.get("OSSRH_PASSWORD") as? String? ?: return@sonatype)
        }
    }
}
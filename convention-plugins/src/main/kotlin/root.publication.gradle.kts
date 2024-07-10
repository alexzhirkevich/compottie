plugins {
    id("io.github.gradle-nexus.publish-plugin")
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

            if (System.getenv("OSSRH_PASSWORD") != null) {
                username.set(System.getenv("OSSRH_USERNAME"))
                password.set(System.getenv("OSSRH_PASSWORD"))
            } else if (findProperty("OSSRH_PASSWORD") != null) {
                username.set(findProperty("OSSRH_USERNAME") as String)
                password.set(findProperty("OSSRH_PASSWORD") as String)
            }
        }
    }
}
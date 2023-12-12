plugins {
    id("org.jetbrains.compose")
    kotlin("multiplatform")
}


val copyJsResources = tasks.create("copyJsResourcesWorkaround", Copy::class.java) {
    from(project(":example:shared").file("src/commonMain/resources"))
    into("build/processedResources/js/main")
}

afterEvaluate {
    project.tasks.getByName("jsProcessResources").finalizedBy(copyJsResources)
}

kotlin {
    js(IR){
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(project(":example:shared"))
            }
        }
    }
}

compose.experimental.web.application{}

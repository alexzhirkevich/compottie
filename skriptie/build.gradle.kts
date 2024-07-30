
plugins {
    id("module.android")
    id("module.multiplatform")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

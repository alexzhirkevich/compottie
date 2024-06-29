plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.nexus.publish)
    implementation(libs.kotlin.gp)
    implementation(libs.android.gp)
}
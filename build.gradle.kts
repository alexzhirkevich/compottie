plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.dokka).apply(false)
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

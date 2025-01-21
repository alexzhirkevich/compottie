package io.github.alexzhirkevich.compottie.internal.assets

internal interface LottieFileAsset : LottieAsset {
    val path : String
    val fileName : String?
    val embedded: Boolean
}
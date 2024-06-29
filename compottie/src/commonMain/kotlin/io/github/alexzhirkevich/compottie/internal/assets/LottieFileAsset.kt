package io.github.alexzhirkevich.compottie.internal.assets

import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt

internal interface LottieFileAsset : LottieAsset {
    val path : String
    val fileName : String?
    val embedded : BooleanInt
    val slotId : String?
}
package io.github.alexzhirkevich.compottie.internal.schema.assets

import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt

internal interface LottieFileAsset : LottieAsset {
    val path : String
    val fileName : String?
    val embedded : BooleanInt
    val slotId : String?
}
package io.github.alexzhirkevich.compottie.internal.services

import io.github.alexzhirkevich.compottie.internal.schema.assets.LottieAsset

internal class LottieAssetService(
    val maintainOriginalImageBounds : Boolean,
    assets : List<LottieAsset>
) : LottieService {

    private val assets = assets.associateBy { it.id }

    fun asset(id : String) : LottieAsset? {
        return assets[id]
    }
}
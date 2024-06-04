package io.github.alexzhirkevich.compottie.internal.assets

import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.Serializable

@Serializable
internal class PrecompositionAsset(
    override val id: String,
    val layers : List<Layer>
) : LottieAsset

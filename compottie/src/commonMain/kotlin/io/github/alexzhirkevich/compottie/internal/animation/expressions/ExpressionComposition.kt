package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.assets.PrecompositionAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer

internal interface ExpressionComposition {

    val name : String?

    val width: Float

    val height: Float

    val startTime: Float

    val durationFrames: Float

    val layers: Map<String, Layer>

    val layersCount : Int
}

internal class ExpressionCompositionFromAsset(
    private val asset: PrecompositionAsset
) : ExpressionComposition {

    override val width: Float
        get() = error("'width' property is available only for the main composition and thisComp")
    override val height: Float
        get() = error("'height' property is available only for the main composition and thisComp")
    override val startTime: Float
        get() = error("'startTime' property is available only for the main composition and thisComp")
    override val durationFrames: Float
        get() = error("'durationFrames' property is available only for the main composition and thisComp")

    override val name: String?
        get() = asset.name

    override val layers: Map<String, Layer> = asset.layers.associateBy { it.name.orEmpty() }

    override val layersCount: Int
        get() = asset.layers.size

}
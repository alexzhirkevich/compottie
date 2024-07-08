package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.PrecompositionAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.utils.IdentityMatrix

internal interface ExpressionComposition {

    val name : String?

    val width: Float

    val height: Float

    val startTime: Float

    val layersByName: Map<String, Layer>

    val layersByIndex: Map<Int, Layer>

    val layersCount : Int

    fun transformMatrix(state: AnimationState) : Matrix = IdentityMatrix
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

    override val name: String?
        get() = asset.name

    override val layersByName: Map<String, Layer> =
        asset.layers.associateBy { it.name.orEmpty() }

    override val layersByIndex: Map<Int, Layer> =
        asset.layers.associateBy { it.index ?: Int.MIN_VALUE }
    override val layersCount: Int
        get() = asset.layers.size

}
package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.PrecompositionAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.utils.IdentityMatrix
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.js

internal interface ExpressionComposition : JsAny {

    val name : String?

    val width: Float

    val height: Float

    val startTime: Float

    val layersByName: Map<String, Layer>

    val layersByIndex: Map<Int, Layer>

    val layersCount : Int

    fun transformMatrix(state: AnimationState) : Matrix = IdentityMatrix

    override suspend fun keys(
        runtime: ScriptRuntime,
        excludeSymbols: Boolean,
        excludeNonEnumerables: Boolean
    ): List<JsAny?> = listOf(
        "name".js(),
        "width".js(),
        "height".js(),
        "startTime".js(),
        "frameDuration".js(),
        "numLayers".js(),
        "layer".js(),
    )

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when (property?.toString()){
            "name" -> name?.js()
            "width" -> width.js()
            "height" -> height.js()
            "displayStartTime" -> startTime.js()
            "frameDuration" -> (runtime.state.composition.frameRate / 1000).js()
            "numLayers" -> layersCount.js()
            "layer" -> Callable {
                val index = it[0]?.toKotlin(this)
                if (index is Number){
                    state.thisComp.layersByIndex[index.toInt()]
                } else {
                    state.thisComp.layersByName[index.toString()]
                }
            }
            else -> super.get(property, runtime)
        }
    }
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
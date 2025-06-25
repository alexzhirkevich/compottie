package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.totalTransformMatrix
import io.github.alexzhirkevich.compottie.internal.timeSeconds
import io.github.alexzhirkevich.compottie.internal.utils.fastSetFrom
import io.github.alexzhirkevich.keight.Callable
import io.github.alexzhirkevich.keight.js.JsAny

internal fun JSLayerToCompOrWorld(
    layer: Layer,
    reverse: Boolean,
    toComp : Boolean,
) = Callable {
    val point = (it[0] as List<JsAny?>).fastMap { toNumber(it) }
    val time = it.getOrNull(1)?.let { toNumber(it) } ?: state.timeSeconds

    if (it.size < 2){
        convert(layer, point, state, reverse, toComp)
    } else {
        state.onTime(time.toFloat()){
            convert(layer, point, it, reverse, toComp)
        }
    }
}

private val conversionMatrix = Matrix()

private fun convert(
    layer: Layer,
    point : List<Number>,
    state: AnimationState,
    reverse: Boolean,
    toComp : Boolean,
) : JsAny {

    val layerMatrix = layer.totalTransformMatrix(state, toComp = toComp)
    val compMatrix = state.thisComp.transformMatrix(state)

    val offset = Offset(
        point[0].toFloat(),
        point[1].toFloat()
    )

    if (reverse){
        conversionMatrix.fastSetFrom(layerMatrix)
        compMatrix.invert()
        conversionMatrix.timesAssign(compMatrix)
    } else {
        conversionMatrix.fastSetFrom(compMatrix)
        layerMatrix.invert()
        conversionMatrix.timesAssign(layerMatrix)
    }

    val result = conversionMatrix.map(offset)

    return listOf(result.x.toJs(), result.y.toJs()).toJs()
}
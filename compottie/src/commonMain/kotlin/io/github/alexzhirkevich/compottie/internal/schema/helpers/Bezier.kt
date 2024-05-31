package io.github.alexzhirkevich.compottie.internal.schema.helpers

import androidx.compose.ui.geometry.Offset
import io.github.alexzhirkevich.compottie.internal.schema.helpers.CubicCurveData
import io.github.alexzhirkevich.compottie.internal.schema.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.schema.util.toOffset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class Bezier(

    @SerialName("c")
    val isClosed : Boolean = false,

    @SerialName("i")
    val inTangents : List<FloatArray> ,

    @SerialName("o")
    val outTangents : List<FloatArray>,

    @SerialName("v")
    val vertices : List<FloatArray>,
)

internal fun Bezier.toShapeData() : ShapeData {

    val curves = vertices.indices.map { idx ->
        CubicCurveData(
            controlPoint1 = inTangents[idx].toOffset(),
            controlPoint2 = outTangents[idx].toOffset(),
            vertex = vertices[idx].toOffset(),
        )
    }.toMutableList()

    return ShapeData(
        curves = curves,
        isClosed = isClosed,
        initialPoint = curves.getOrNull(0)?.vertex ?: Offset.Zero
    )
}
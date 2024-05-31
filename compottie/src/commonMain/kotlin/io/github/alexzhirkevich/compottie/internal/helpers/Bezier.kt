package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.util.toOffset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.min

@Serializable
internal class Bezier(

    @SerialName("c")
    var isClosed : Boolean = false,

    @SerialName("i")
    var inTangents : List<FloatArray> = emptyList(),

    @SerialName("o")
    var outTangents : List<FloatArray> = emptyList(),

    @SerialName("v")
    var vertices : List<FloatArray> = emptyList(),
) {
    init {
        require(vertices.size == inTangents.size && vertices.size == outTangents.size){
            "Invalid bezier curve. Control points count must be the same as vertices count"
        }
    }
}

internal fun Bezier.lerp(from: Bezier, to: Bezier, fraction : Float) {
    isClosed = from.isClosed || to.isClosed

    val size = min(from.vertices.size, to.vertices.size)

    if (inTangents !is MutableList<*>) {
        inTangents = ArrayList(size)
    }

    if (outTangents !is MutableList<*>) {
        outTangents = ArrayList(size)
    }

    if (vertices !is MutableList<*>) {
        vertices = ArrayList(size)
    }

    val i = inTangents as MutableList<FloatArray>
    val o = outTangents as MutableList<FloatArray>
    val v = vertices as MutableList<FloatArray>

    if(v.size > size){
        v.dropLast(v.size-size)
        o.dropLast(v.size-size)
        i.dropLast(v.size-size)
    }

    if (v.size < size){
        repeat(size - v.size){
            v.add(floatArrayOf(0f,0f))
            o.add(floatArrayOf(0f,0f))
            i.add(floatArrayOf(0f,0f))
        }
    }

    repeat(size) {

        val cp11 = from.inTangents[it].toOffset()
        val cp21 = from.outTangents[it].toOffset()
        val vertex1 = from.vertices[it].toOffset()

        val cp12 = to.inTangents[it].toOffset()
        val cp22 = to.outTangents[it].toOffset()
        val vertex2 = to.vertices[it].toOffset()

        i[it][0] = androidx.compose.ui.util.lerp(cp11.x, cp12.x, fraction)
        i[it][1] = androidx.compose.ui.util.lerp(cp11.y, cp12.y, fraction)

        o[it][0] = androidx.compose.ui.util.lerp(cp21.x, cp22.x, fraction)
        o[it][1] = androidx.compose.ui.util.lerp(cp21.y, cp22.y, fraction)

        v[it][0] = androidx.compose.ui.util.lerp(vertex1.x, vertex2.x, fraction)
        v[it][1] = androidx.compose.ui.util.lerp(vertex1.y, vertex2.y, fraction)
    }
}

internal fun Bezier.toShapeData() : ShapeData {

    val initialPoint = vertices.first()

    val curves = mutableListOf<CubicCurveData>()

    for (i in 1..vertices.lastIndex) {

        val prevVertex = vertices[i - 1]
        val cp1 = outTangents[i - 1]
        val cp2 = inTangents[i]
        val vertex = vertices[i]

        val shapeCp1 = floatArrayOf(prevVertex[0] + cp1[0], prevVertex[1] + cp1[1])
        val shapeCp2 = floatArrayOf(vertex[0] + cp2[0], vertex[1] + cp2[1])
        curves.add(CubicCurveData(shapeCp1.toOffset(), shapeCp2.toOffset(), vertex.toOffset()))
    }

    if (isClosed) {
        val vertex = vertices[0]
        val prevVertex = vertices.last()
        val cp1 = outTangents[vertices.lastIndex]
        val cp2 = inTangents[0]

        val shapeCp1 = floatArrayOf(prevVertex[0] + cp1[0], prevVertex[1] + cp1[1])
        val shapeCp2 = floatArrayOf(vertex[0] + cp2[0], vertex[1] + cp2[1])

        curves.add(CubicCurveData(shapeCp1.toOffset(), shapeCp2.toOffset(), vertex.toOffset()))
    }

    return ShapeData(
        curves = curves,
        initialPoint = initialPoint.toOffset(),
        isClosed = isClosed
    )
}

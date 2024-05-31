package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.Offset
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.ShapeModifierContent
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.helpers.CubicCurveData
import io.github.alexzhirkevich.compottie.internal.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.utils.floorMod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.hypot
import kotlin.math.min

@Serializable
@SerialName("rd")
internal class RoundShape(
    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    override val hidden : Boolean = false,

    @SerialName("r")
    val radius : AnimatedValue,
) : Shape, ShapeModifierContent {

    @Transient
    private var shapeData: ShapeData? = null
    override fun modify(shape: ShapeData, frame: Float): ShapeData {
        val startingCurves: List<CubicCurveData> = shape.curves

        if (startingCurves.size <= 2) {
            return shape
        }
        val roundedness: Float = radius.interpolated(frame)
        if (roundedness == 0f) {
            return shape
        }

        val modifiedShapeData = getShapeData(shape)
        modifiedShapeData.initialPoint = shape.initialPoint
        val modifiedCurves = modifiedShapeData.curves
        var modifiedCurvesIndex = 0
        val isClosed = shape.isClosed

        // i represents which vertex we are currently on. Refer to the docs of CubicCurveData prior to working with
        // this code.
        // When i == 0
        //    vertex=ShapeData.initialPoint
        //    inCp=if closed vertex else curves[size - 1].cp2
        //    outCp=curves[0].cp1
        // When i == 1
        //    vertex=curves[0].vertex
        //    inCp=curves[0].cp2
        //    outCp=curves[1].cp1.
        // When i == size - 1
        //    vertex=curves[size - 1].vertex
        //    inCp=curves[size - 1].cp2
        //    outCp=if closed vertex else curves[0].cp1
        for (i in startingCurves.indices) {
            val startingCurve = startingCurves[i]
            val previousCurve =
                startingCurves[floorMod(i - 1, startingCurves.size)]
            val previousPreviousCurve =
                startingCurves[floorMod(i - 2, startingCurves.size)]
            val vertex = if ((i == 0 && !isClosed)) shape.initialPoint else previousCurve.vertex
            val inPoint = if ((i == 0 && !isClosed)) vertex else previousCurve.controlPoint2
            val outPoint = startingCurve.controlPoint1
            val previousVertex = previousPreviousCurve.vertex
            val nextVertex = startingCurve.vertex

            // We can't round the corner of the end of a non-closed curve.
            val isEndOfCurve = !shape.isClosed && (i == 0 || i == startingCurves.size - 1)
            if (inPoint == vertex && outPoint == vertex && !isEndOfCurve) {
                // This vertex is a point. Round its corners
                val dxToPreviousVertex: Float = vertex.x - previousVertex.x
                val dyToPreviousVertex: Float = vertex.y - previousVertex.y
                val dxToNextVertex: Float = nextVertex.x - vertex.x
                val dyToNextVertex: Float = nextVertex.y - vertex.y

                val dToPreviousVertex =
                    hypot(dxToPreviousVertex.toDouble(), dyToPreviousVertex.toDouble()).toFloat()
                val dToNextVertex =
                    hypot(dxToNextVertex.toDouble(), dyToNextVertex.toDouble()).toFloat()

                val previousVertexPercent =
                    min((roundedness / dToPreviousVertex).toDouble(), 0.5).toFloat()
                val nextVertexPercent =
                    min((roundedness / dToNextVertex).toDouble(), 0.5).toFloat()

                // Split the vertex into two and move each vertex towards the previous/next vertex.
                val newVertex1X: Float =
                    vertex.x + (previousVertex.x - vertex.x) * previousVertexPercent
                val newVertex1Y: Float =
                    vertex.y + (previousVertex.y - vertex.y) * previousVertexPercent
                val newVertex2X: Float = vertex.x + (nextVertex.x - vertex.x) * nextVertexPercent
                val newVertex2Y: Float = vertex.y + (nextVertex.y - vertex.y) * nextVertexPercent

                // Extend the new vertex control point towards the original vertex.
                val newVertex1OutPointX: Float =
                    newVertex1X - (newVertex1X - vertex.x) * ROUNDED_CORNER_MAGIC_NUMBER
                val newVertex1OutPointY: Float =
                    newVertex1Y - (newVertex1Y - vertex.y) * ROUNDED_CORNER_MAGIC_NUMBER
                val newVertex2InPointX: Float =
                    newVertex2X - (newVertex2X - vertex.x) * ROUNDED_CORNER_MAGIC_NUMBER
                val newVertex2InPointY: Float =
                    newVertex2Y - (newVertex2Y - vertex.y) * ROUNDED_CORNER_MAGIC_NUMBER

                // Remap vertex/in/out point to CubicCurveData.
                // Refer to the docs for CubicCurveData for more info on the difference.
                var previousCurveData = modifiedCurves[floorMod(modifiedCurvesIndex - 1, modifiedCurves.size)]
                var currentCurveData = modifiedCurves[modifiedCurvesIndex]
                previousCurveData.controlPoint2 = Offset(newVertex1X, newVertex1Y)
                previousCurveData.vertex = Offset(newVertex1X, newVertex1Y)
                if (i == 0) {
                    modifiedShapeData.initialPoint = Offset(newVertex1X, newVertex1Y)
                }
                currentCurveData.controlPoint1 = Offset(newVertex1OutPointX, newVertex1OutPointY)
                modifiedCurvesIndex++

                previousCurveData = currentCurveData
                currentCurveData = modifiedCurves[modifiedCurvesIndex]
                previousCurveData.controlPoint2 = Offset(newVertex2InPointX, newVertex2InPointY)
                previousCurveData.vertex = Offset(newVertex2X, newVertex2Y)
                currentCurveData.controlPoint1 = Offset(newVertex2X, newVertex2Y)
                modifiedCurvesIndex++
            } else {
                // This vertex is not a point. Don't modify it. Refer to the documentation above and for CubicCurveData for mapping a vertex
                // oriented point to CubicCurveData (path segments).
                val previousCurveData =
                    modifiedCurves[floorMod(modifiedCurvesIndex - 1, modifiedCurves.size)]
                val currentCurveData = modifiedCurves[modifiedCurvesIndex]
                previousCurveData.controlPoint2 = previousCurve.controlPoint2
                previousCurveData.vertex = previousCurve.vertex
                currentCurveData.controlPoint1 = previousCurve.controlPoint1
                modifiedCurvesIndex++
            }
        }
        return modifiedShapeData
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }

    private fun getShapeData(startingShapeData: ShapeData): ShapeData {
        val startingCurves: List<CubicCurveData> = startingShapeData.curves
        val isClosed: Boolean = startingShapeData.isClosed
        var vertices = 0

        for (i in startingCurves.indices.reversed()) {
            val startingCurve: CubicCurveData = startingCurves[i]
            val previousCurve: CubicCurveData =
                startingCurves[floorMod(i - 1, startingCurves.size)]
            val vertex = if ((i == 0 && !isClosed))
                startingShapeData.initialPoint
            else previousCurve.vertex

            val inPoint = if ((i == 0 && !isClosed))
                vertex else previousCurve.controlPoint2
            val outPoint = startingCurve.controlPoint1

            val isEndOfCurve =
                !startingShapeData.isClosed && (i == 0 || i == startingCurves.size - 1)

            vertices += if (inPoint == vertex && outPoint == vertex && !isEndOfCurve) {
                2
            } else {
                1
            }
        }
        if (shapeData?.curves?.size != vertices) {
            val newCurves: MutableList<CubicCurveData> = MutableList(vertices) {
                CubicCurveData()
            }

            shapeData = ShapeData(newCurves, Offset.Zero, false)
        }
        shapeData?.isClosed = isClosed
        return shapeData!!
    }
}

private const val ROUNDED_CORNER_MAGIC_NUMBER = 0.5519f

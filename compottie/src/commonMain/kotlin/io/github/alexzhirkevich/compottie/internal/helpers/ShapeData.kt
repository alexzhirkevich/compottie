package io.github.alexzhirkevich.compottie.internal.helpers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.lerp
import kotlin.math.min

internal class ShapeData(
    var curves: MutableList<CubicCurveData> = mutableListOf(),
    var initialPoint: Offset = Offset.Zero,
    var isClosed: Boolean = false,
) {

    fun interpolateBetween(
        shapeData1: ShapeData,
        shapeData2: ShapeData,
        percentage: Float,
    ) {
        isClosed = shapeData1.isClosed || shapeData2.isClosed

        val points = min(shapeData1.curves.size, shapeData2.curves.size)

        if (curves.size < points) {
            for (i in curves.size until points) {
                curves.add(CubicCurveData())
            }
        } else if (curves.size > points) {
            for (i in curves.size - 1 downTo points) {
                curves.removeAt(curves.size - 1)
            }
        }

        val initialPoint1 = shapeData1.initialPoint
        val initialPoint2 = shapeData2.initialPoint

        initialPoint = Offset(
            lerp(initialPoint1.x, initialPoint2.x, percentage),
            lerp(initialPoint1.y, initialPoint2.y, percentage)
        )

        for (i in curves.indices.reversed()) {
            val curve1: CubicCurveData = shapeData1.curves[i]
            val curve2: CubicCurveData = shapeData2.curves[i]

            val cp11 = curve1.controlPoint1
            val cp21 = curve1.controlPoint2
            val vertex1 = curve1.vertex

            val cp12 = curve2.controlPoint1
            val cp22 = curve2.controlPoint2
            val vertex2 = curve2.vertex

            curves[i].controlPoint1 = Offset(
              lerp(cp11.x, cp12.x, percentage),
              lerp(cp11.y, cp12.y, percentage)
            )
            curves[i].controlPoint2 = Offset(
              lerp(cp21.x, cp22.x, percentage),
              lerp(cp21.y, cp22.y, percentage)
            )
            curves[i].vertex = Offset(
              lerp(vertex1.x, vertex2.x, percentage),
              lerp(vertex1.y, vertex2.y, percentage)
            )
        }
    }
}

internal fun ShapeData.mapPath(outPath : Path) {
    outPath.reset()
    outPath.moveTo(initialPoint.x, initialPoint.y)

    var pathFromDataCurrentPoint = initialPoint
    for (i in curves.indices) {
        val curveData = curves[i]
        val cp1 = curveData.controlPoint1
        val cp2 = curveData.controlPoint2
        val vertex = curveData.vertex

        if (cp1 == pathFromDataCurrentPoint&& cp2 == vertex) {
            // On some phones like Samsung phones, zero valued control points can cause artifacting.
            // https://github.com/airbnb/lottie-android/issues/275
            //
            // This does its best to add a tiny value to the vertex without affecting the final
            // animation as much as possible.
            // outPath.rMoveTo(0.01f, 0.01f);
            outPath.lineTo(vertex.x, vertex.y)
        } else {
            outPath.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, vertex.x, vertex.y)
        }
        pathFromDataCurrentPoint = vertex
    }
    if (isClosed) {
        outPath.close()
    }
}
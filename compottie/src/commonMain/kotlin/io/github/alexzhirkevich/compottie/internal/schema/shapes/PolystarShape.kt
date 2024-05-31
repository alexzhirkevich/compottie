package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.CompoundTrimPath
import io.github.alexzhirkevich.compottie.internal.utils.Math
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmInline
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@Serializable
@JvmInline
internal value class StarType(val type : Byte) {
    companion object {
        val Star = StarType(1)
        val Polygon = StarType(2)
    }
}

@Serializable
@SerialName("sr")
internal class PolystarShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("p")
    val position : AnimatedVector2?,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("is")
    val innerRoundness : AnimatedValue?,

    @SerialName("ir")
    val innerRadius : AnimatedValue,

    @SerialName("or")
    val outerRadius : AnimatedValue,

    @SerialName("os")
    val outerRoundness : AnimatedValue?,

    @SerialName("r")
    val rotation : AnimatedValue?,

    @SerialName("pt")
    val points : AnimatedValue,

    @SerialName("sy")
    val starType : StarType,

) : Shape, PathContent {

    @Transient
    private val path = Path()

    @Transient
    private val lastSegmentPath = Path()

    @Transient
    private val lastSegmentPathMeasure = PathMeasure()

    @Transient
    private val trimPaths = CompoundTrimPath()

    override fun getPath(frame: Int): Path {

        path.reset()

        when (starType) {
            StarType.Star -> createStarPath(frame)
            StarType.Polygon -> createPolygonPath(frame)
        }

        path.close()

        trimPaths.apply(path, frame)

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentsBefore.fastForEach {
            if (it.isSimultaneousTrimPath()) {
                trimPaths.addTrimPath(it)
            }
        }
    }

    private fun createStarPath(frame: Int) {
        val points = points.interpolated(frame = frame)
        var currentAngle = Math.toRadians((rotation?.interpolated(frame) ?: 0f) - 90f)

        // adjust current angle for partial points
        val anglePerPoint: Float = (TwoPI / points).toFloat()
//        if (isReversed) {
//            anglePerPoint *= -1f
//        }
        val halfAnglePerPoint = anglePerPoint / 2.0f
        val partialPointAmount = points - points.toInt()
        if (partialPointAmount != 0f) {
            currentAngle += (halfAnglePerPoint * (1f - partialPointAmount))
        }

        val outerRadius = outerRadius.interpolated(frame)
        val innerRadius = innerRadius.interpolated(frame)

        val innerRoundedness = innerRoundness?.interpolated(frame)?.div(100f) ?: 0f
        val outerRoundedness = outerRoundness?.interpolated(frame)?.div(100f) ?: 0f

        var x: Float
        var y: Float
        var previousX: Float
        var previousY: Float
        var partialPointRadius = 0f

        if (partialPointAmount != 0f) {
            partialPointRadius = innerRadius + partialPointAmount * (outerRadius - innerRadius)
            x = partialPointRadius * cos(currentAngle)
            y = partialPointRadius * sin(currentAngle)
            path.moveTo(x, y)
            currentAngle += (anglePerPoint * partialPointAmount / 2f)
        } else {
            x = outerRadius * cos(currentAngle)
            y = outerRadius * sin(currentAngle)
            path.moveTo(x, y)
            currentAngle += halfAnglePerPoint
        }


        // True means the line will go to outer radius. False means inner radius.
        var longSegment = false
        val numPoints = (ceil(points) * 2).toInt()
        repeat(numPoints) { i ->
            var radius = if (longSegment) outerRadius else innerRadius
            var dTheta = halfAnglePerPoint
            if (partialPointRadius != 0f && i == numPoints - 2) {
                dTheta = anglePerPoint * partialPointAmount / 2f
            }
            if (partialPointRadius != 0f && i == numPoints - 1) {
                radius = partialPointRadius
            }
            previousX = x
            previousY = y
            x = radius * cos(currentAngle)
            y = radius * sin(currentAngle)

            if (innerRoundedness == 0f && outerRoundedness == 0f) {
                path.lineTo(x, y)
            } else {
                val cp1Theta = (atan2(previousY, previousX) - HalfPI)
                val cp1Dx = cos(cp1Theta).toFloat()
                val cp1Dy = sin(cp1Theta).toFloat()

                val cp2Theta = (atan2(y, x) - HalfPI)
                val cp2Dx = cos(cp2Theta).toFloat()
                val cp2Dy = sin(cp2Theta).toFloat()

                val cp1Roundedness = if (longSegment) innerRoundedness else outerRoundedness
                val cp2Roundedness = if (longSegment) outerRoundedness else innerRoundedness
                val cp1Radius = if (longSegment) innerRadius else outerRadius
                val cp2Radius = if (longSegment) outerRadius else innerRadius

                var cp1x: Float =
                    cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dx
                var cp1y: Float =
                    cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dy
                var cp2x: Float =
                    cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dx
                var cp2y: Float =
                    cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dy

                if (partialPointAmount != 0f) {
                    if (i == 0) {
                        cp1x *= partialPointAmount
                        cp1y *= partialPointAmount
                    } else if (i == numPoints - 1) {
                        cp2x *= partialPointAmount
                        cp2y *= partialPointAmount
                    }
                }

                path.cubicTo(previousX - cp1x, previousY - cp1y, x + cp2x, y + cp2y, x, y)
            }

            currentAngle += dTheta
            longSegment = !longSegment
        }


        val position = position?.interpolated(frame)?.let {
            path.translate(it)
        }

        path.close()
    }

    private fun createPolygonPath(frame: Int) {
        val points = floor(points.interpolated(frame)).toInt()
        var currentAngle = Math.toRadians((rotation?.interpolated(frame) ?: 0f) - 90f)

        // adjust current angle for partial points
        val anglePerPoint = (TwoPI / points).toFloat()

        val roundedness = outerRoundness?.interpolated(frame)?.div(100f) ?: 0f
        val radius = outerRadius.interpolated(frame)
        var x: Float
        var y: Float
        var previousX: Float
        var previousY: Float
        x = radius * cos(currentAngle)
        y = radius * sin(currentAngle)
        path.moveTo(x, y)
        currentAngle += anglePerPoint


        repeat(points) { i ->

            previousX = x
            previousY = y
            x = radius * cos(currentAngle)
            y = radius * sin(currentAngle)

            if (roundedness != 0f) {
                val cp1Theta = (atan2(previousY, previousX) - HalfPI)
                val cp1Dx = cos(cp1Theta)
                val cp1Dy = sin(cp1Theta)

                val cp2Theta = atan2(y, x) - HalfPI
                val cp2Dx = cos(cp2Theta).toFloat()
                val cp2Dy = sin(cp2Theta).toFloat()

                val cp1x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dx
                val cp1y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dy
                val cp2x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dx
                val cp2y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dy


                val vX = (previousX - cp1x).toFloat()
                val vY = (previousY - cp1y).toFloat()
                val cpX = x + cp2x
                val cpY = y + cp2y

                if (i == points - 1) {
                    // When there is a huge stroke, it will flash if the path ends where it starts.
                    // We want the final bezier curve to end *slightly* before the start.
                    // The close() call at the end will complete the polystar.
                    // https://github.com/airbnb/lottie-android/issues/2329
                    lastSegmentPath.reset()
                    lastSegmentPath.moveTo(previousX, previousY)
                    lastSegmentPath.cubicTo(vX, vY, cpX, cpY, x, y)
                    lastSegmentPathMeasure.setPath(lastSegmentPath, false)

                    val lastSegmentPosition =
                        lastSegmentPathMeasure.getPosition(lastSegmentPathMeasure.length * 0.9999f)
                    path.cubicTo(
                        vX,
                        vY,
                        cpX,
                        cpY,
                        lastSegmentPosition.x,
                        lastSegmentPosition.y
                    )
                } else {
                    path.cubicTo(vX, vY, x + cp2x, y + cp2y, x, y)
                }
            } else {
                if (i != points - 1) {
                    // When there is a huge stroke, it will flash if the path ends where it starts.
                    // The close() call should make the path effectively equivalent.
                    // https://github.com/airbnb/lottie-android/issues/2329
                    path.lineTo(x, y)
                }
            }

            currentAngle += anglePerPoint
        }

        position?.interpolated(frame)?.takeIf { it != Offset.Zero }?.let {
            path.translate(it)
        }
        path.close()
    }
}

private const val HalfPI = PI /2f
private const val TwoPI = PI * 2f
private const val POLYSTAR_MAGIC_NUMBER = .47829f
private const val POLYGON_MAGIC_NUMBER = .25f

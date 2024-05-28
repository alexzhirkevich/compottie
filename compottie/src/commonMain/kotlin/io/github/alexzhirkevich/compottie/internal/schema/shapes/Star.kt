package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ScaleFactor
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmInline
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Serializable
@SerialName("sr")
internal class Star(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("p")
    val position : AnimatedVector2,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("is")
    val innerRoundness : AnimatedValue,

    @SerialName("ir")
    val innerRadius : AnimatedValue,

    @SerialName("or")
    val outerRadius : AnimatedValue,

    @SerialName("os")
    val outerRoundness : AnimatedValue,

    @SerialName("r")
    val rotation : AnimatedValue,

    @SerialName("pt")
    val points : AnimatedValue,

    @SerialName("s")
    val size : AnimatedVector2,

    @SerialName("sy")
    val starType : StarType,

    ) : LayoutShape {

    @Transient
    private val path = Path()

    override fun getPath(time: Int): Path {

        path.rewind()

        val position = position.interpolated(time)
        val size = size.interpolated(time)

        val top = (position[0] - size[0] / 2)
        val left = (position[1] - size[0] / 2)

        path.addOval(
            Rect(
                top = top,
                left = left,
                bottom = top + size[0],
                right = left + size[1],
            )
        )
        return path
    }

    private fun applyStarTo(path: Path, scale: ScaleFactor, time: Int) {

        val points = points.interpolated(time)
        val rotation = rotation.interpolated(time)
        val outer_roundness = outerRoundness.interpolated(time)
        val inner_roundness = innerRoundness.interpolated(time)
        val outer_radius = outerRadius.interpolated(time)
        val inner_radius = innerRadius.interpolated(time)

        val half_angle = PI / points
        val angle_radians = rotation / 180 * PI

        // Tangents for rounded courners
        val tangent_len_outer =
            (outer_roundness * outer_radius * 2 * PI / (points * 4 * 100)).toFloat()
        val tangent_len_inner =
            (inner_roundness * inner_radius * 2 * PI / (points * 4 * 100)).toFloat()

        repeat(points.toInt()) { i ->
            val main_angle = -PI / 2 + angle_radians + i * half_angle * 2;

            val outer_vertex = Offset(
                outer_radius * cos(main_angle).toFloat(),
                outer_radius * sin(main_angle).toFloat()
            );

            val outer_tangent = if (outer_radius == 0f) {
                Offset.Zero
            } else {
                Offset(
                    outer_vertex.y / outer_radius * tangent_len_outer,
                    -outer_vertex.x / outer_radius * tangent_len_outer
                )
            }

            path.cubicTo(
                outer_vertex.x,
                outer_vertex.y,
                outer_tangent.x,
                outer_tangent.y,
                -outer_tangent.x,
                -outer_tangent.y
            )

            // Star inner radius
            if (starType == StarType.Star) {
                val inner_vertex = Offset(
                    inner_radius * cos(main_angle + half_angle).toFloat(),
                    inner_radius * sin(main_angle + half_angle).toFloat()
                )

                val inner_tangent = if (inner_radius.toInt() != 0) {
                    Offset(
                        inner_vertex.y / inner_radius * tangent_len_inner,
                        -inner_vertex.x / inner_radius * tangent_len_inner
                    )
                } else {
                    Offset.Zero
                }

                path.cubicTo(
                    inner_vertex.x,
                    inner_vertex.y,
                    inner_tangent.x,
                    inner_tangent.y,
                    -inner_tangent.x,
                    -inner_tangent.y
                )
            }
        }
    }
}

@Serializable
@JvmInline
value class StarType(val type : Byte) {
    companion object {
        val Star = StarType(1)
        val Polygon = StarType(2)
    }
}
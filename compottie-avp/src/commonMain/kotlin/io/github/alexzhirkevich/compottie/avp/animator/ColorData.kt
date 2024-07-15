package io.github.alexzhirkevich.compottie.avp.animator

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.TileMode

public sealed interface ColorData {

    public sealed class GradientColorData : ColorData {

        public abstract val colorStops: List<Pair<Float, Color>>

        protected val colors : List<Color> get() = mColors

        protected val stops : List<Float> get() = mStops


        private val mColors by lazy {
            colorStops.map { it.second }.toMutableList()
        }

        private val mStops by lazy {
            colorStops.map { it.first }.toMutableList()
        }

        internal abstract val shader : Shader

        internal abstract fun lerpShader(other : GradientColorData, progress: Float) : Shader

        protected fun lerpColorStops(other: GradientColorData, progress: Float) {
            require(other.colorStops.size == colorStops.size){
                repeat(colorStops.size) {
                    mColors[it] = androidx.compose.ui.graphics.lerp(
                        colorStops[it].second,
                        other.colorStops[it].second,
                        progress
                    )
                    mStops[it] = androidx.compose.ui.util.lerp(
                        colorStops[it].first,
                        other.colorStops[it].first,
                        progress
                    )
                }
            }
        }
    }

    public class Solid(public val color: Color) : ColorData

    public class LinearGradient(
        override val colorStops : List<Pair<Float, Color>>,
        public val start : Offset,
        public val end : Offset,
        public val tileMode: TileMode
    ) : GradientColorData() {

        override val shader: Shader by lazy {
            LinearGradientShader(
                from = start,
                to = end,
                colors = colors,
                colorStops = stops
            )
        }

        override fun lerpShader(other: GradientColorData, progress: Float): Shader {
            other as LinearGradient
            lerpColorStops(other, progress)

            return LinearGradientShader(
                from = androidx.compose.ui.geometry.lerp(start, other.start, progress),
                to = androidx.compose.ui.geometry.lerp(end, other.end, progress),
                colors = colors,
                colorStops = stops,
                tileMode = other.tileMode
            )
        }
    }

    public class RadialGradient(
        override val colorStops : List<Pair<Float, Color>>,
        public val center : Offset,
        public val radius : Float,
        public val tileMode: TileMode
    ) : GradientColorData() {

        override val shader: Shader by lazy {
            RadialGradientShader(
                center = center,
                radius = radius,
                colors = colors,
                colorStops = stops
            )
        }

        override fun lerpShader(other: GradientColorData, progress: Float): Shader {
            other as RadialGradient
            lerpColorStops(other, progress)

            return RadialGradientShader(
                center = androidx.compose.ui.geometry.lerp(center, other.center, progress),
                radius = androidx.compose.ui.util.lerp(radius, other.radius, progress),
                colors = colors,
                colorStops = stops,
                tileMode = other.tileMode
            )
        }
    }

    public class SweepGradient(
        override val colorStops : List<Pair<Float, Color>>,
        public val center : Offset,
    ) : GradientColorData() {

        override val shader: Shader by lazy {
            SweepGradientShader(
                center = center,
                colors = colors,
                colorStops = stops
            )
        }

        override fun lerpShader(other: GradientColorData, progress: Float): Shader {
            other as SweepGradient
            lerpColorStops(other, progress)

            return SweepGradientShader(
                center = androidx.compose.ui.geometry.lerp(center, other.center, progress),
                colors = colors,
                colorStops = stops,
            )
        }
    }
}
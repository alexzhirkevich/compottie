package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.helpers.mapPath
import io.github.alexzhirkevich.compottie.internal.helpers.toShapeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedShape : KeyframeAnimation<Path>, Indexable {

    @SerialName("0")
    @Serializable
    class Default(
        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("k")
        val bezier: Bezier,
    ) : AnimatedShape {

        private val unmodifiedPath by lazy {
            Path().also {
                bezier.toShapeData().mapPath(it)
            }
        }

        private val shapeData by lazy {
            bezier.toShapeData()
        }

        @Transient
        private val tmpPath = Path()
        override fun interpolated(state: AnimationState): Path {
            tmpPath.reset()
            shapeData.mapPath(tmpPath)
            return tmpPath
        }
    }

    @SerialName("1")
    @Serializable
    class Animated(
        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("k")
        val keyframes: List<BezierKeyframe>,
    ) : AnimatedShape, KeyframeAnimation<Path> {

        @Transient
        private val tmpPath = Path()

        @Transient
        private val tmpShapeData = ShapeData()

        @Transient
        private var delegate =  BaseKeyframeAnimation(
            expression = expression,
            keyframes = keyframes.map { it.toShapeKeyframe() },
            emptyValue = tmpPath,
            map = { s, e, p->
                tmpShapeData.interpolateBetween(s, e, easingX.transform(p))
                tmpShapeData.mapPath(tmpPath)
                tmpPath
            }
        )

        override fun interpolated(state: AnimationState): Path {
            return delegate.interpolated(state)
        }
    }
}



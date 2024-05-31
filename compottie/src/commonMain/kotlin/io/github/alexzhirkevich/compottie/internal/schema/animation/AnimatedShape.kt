package io.github.alexzhirkevich.compottie.internal.schema.animation

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.content.ShapeModifierContent
import io.github.alexzhirkevich.compottie.internal.content.modifiedBy
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.schema.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.schema.helpers.ShapeData
import io.github.alexzhirkevich.compottie.internal.schema.helpers.mapPath
import io.github.alexzhirkevich.compottie.internal.schema.helpers.toShapeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface AnimatedShape : KeyframeAnimation<Path>, Indexable {

    var shapeModifiers: List<ShapeModifierContent>

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

        @Transient
        override var shapeModifiers: List<ShapeModifierContent> = emptyList()

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
        override fun interpolated(frame: Int): Path {
            if (shapeModifiers.isEmpty()) {
                tmpPath.set(unmodifiedPath)
                return unmodifiedPath
            }

            tmpPath.reset()
            shapeData.modifiedBy(shapeModifiers, frame).mapPath(tmpPath)
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
        override var shapeModifiers: List<ShapeModifierContent> = emptyList()

        @Transient
        private val tmpPath = Path()

        @Transient
        private val tmpShapeData = ShapeData()

        @Transient
        private var delegate =  BaseKeyframeAnimation(
            keyframes = keyframes.map { it.toShapeKeyframe() },
            emptyValue = tmpPath,
            map = { s, e, p, frame ->
                tmpShapeData.interpolateBetween(s, e, easingX.transform(p))
                val modified = tmpShapeData.modifiedBy(shapeModifiers, frame)
                modified.mapPath(tmpPath)
                tmpPath
            }
        )

        override fun interpolated(frame: Int): Path {
            return delegate.interpolated(frame)
        }
    }
}



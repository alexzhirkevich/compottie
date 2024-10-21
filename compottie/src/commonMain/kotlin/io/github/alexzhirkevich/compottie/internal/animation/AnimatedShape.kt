package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.Bezier
import io.github.alexzhirkevich.compottie.internal.isNotNull
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = AnimatedShapeSerializer::class)
internal sealed interface AnimatedShape : AnimatedProperty<Path> {

    fun rawBezier(state: AnimationState): Bezier

    fun copy(): AnimatedShape

    fun setClosed(closed: Boolean)


    @Serializable
    class Default(
        @SerialName("x")
        val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,

        @SerialName("k")
        val bezier: Bezier,
    ) : AnimatedShape {

        @Transient
        private val tmpPath = Path()

        override fun setClosed(closed: Boolean) {
            bezier.setIsClosed(closed)
        }

        override fun rawBezier(state: AnimationState): Bezier {
            return bezier
        }

        override fun raw(state: AnimationState): Path {
            bezier.mapPath(tmpPath)
            return tmpPath
        }

        override fun copy(): AnimatedShape {
            return Default(
                expression = expression,
                index = index,
                bezier = bezier
            )
        }
    }

    @Serializable
    class Animated(
        @SerialName("x")
        val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,

        @SerialName("k")
        override val keyframes: List<BezierKeyframe>,
    ) : AnimatedShape, AnimatedKeyframeProperty<Path, BezierKeyframe> {

        @Transient
        private val tmpPath = Path()

        @Transient
        private val tmpBezier = Bezier()

        @Transient
        private var bezierDelegate = BaseKeyframeAnimation(
            index = index,
            keyframes = keyframes,
            emptyValue = tmpBezier,
            map = { s, e, p ->
                tmpBezier.interpolateBetween(s, e, easingX.transform(p))
                tmpBezier
            },
        )

        @Transient
        private var delegate = BaseKeyframeAnimation(
            index = index,
            keyframes = keyframes,
            emptyValue = tmpPath,
            map = { s, e, p ->
                tmpBezier.interpolateBetween(s, e, easingX.transform(p))
                tmpBezier.mapPath(tmpPath)
                tmpPath
            }
        )

        override fun setClosed(closed: Boolean) {
            keyframes.fastForEach {
                it.start?.setIsClosed(closed)
                it.end?.setIsClosed(closed)
            }
        }

        override fun rawBezier(state: AnimationState): Bezier {
            return bezierDelegate.raw(state)
        }

        override fun copy(): AnimatedShape {
            return Animated(
                expression = expression,
                index = index,
                keyframes = keyframes
            )
        }

        override fun raw(state: AnimationState): Path {
            return delegate.raw(state)
        }
    }

    @Serializable
    class Slottable(
        private val sid: String,
        @SerialName("ix")
        override val index: Int? = null,
    ) : AnimatedShape {

        @Transient
        private val emptyBezier = Bezier()

        private val emptyPath by lazy { Path() }

        override fun rawBezier(state: AnimationState): Bezier {
            return state.composition.animation.slots.shape(sid)?.rawBezier(state) ?: emptyBezier
        }

        override fun copy(): AnimatedShape {
            return Slottable(sid, index)
        }

        override fun setClosed(closed: Boolean) {}

        override fun raw(state: AnimationState): Path {
            return state.composition.animation.slots.shape(sid)?.interpolated(state)
                ?: emptyPath.apply { reset() }
        }
    }
}

internal object AnimatedShapeSerializer : JsonContentPolymorphicSerializer<AnimatedShape>(
    baseClass = AnimatedShape::class
){
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedShape> {

        if (element.jsonObject["sid"].isNotNull())
            return AnimatedShape.Slottable.serializer()

        val k = requireNotNull(element.jsonObject["k"]){
            "Animated shape must have 'k' parameter"
        }

        return if (element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 0 || k is JsonObject){
            AnimatedShape.Default.serializer()
        } else {
            AnimatedShape.Animated.serializer()
        }
    }
}


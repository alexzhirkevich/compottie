package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalSerializationApi::class)
@Serializable()
@JsonClassDiscriminator("a")
internal sealed interface AnimatedVector2 : Animated<AnimationVector2D>, Indexable {

    @Serializable
    @SerialName("0")
    data class Default(
        @SerialName("k")
        val value: FloatArray,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedVector2 {

        @Transient
        private val animationVector = AnimationVector(value[0], value[1])

        override fun interpolated(frame: Int): AnimationVector2D = animationVector
    }

    @Serializable
    @SerialName("1")
    class Keyframed(

        @SerialName("k")
        val value: List<VectorKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("ti")
        val inTangent: FloatArray? = null,

        @SerialName("to")
        val outTangent: FloatArray? = null,
    ) : AnimatedVector2 {

        @Transient
        private val animation = value.to2DAnimation()

        override fun interpolated(frame: Int): AnimationVector2D {
            return animation.getValueFromNanos(
                playTimeNanos = frame.milliseconds.inWholeNanoseconds,
            )
        }
    }
}

val AnimationVector2D.x : Float get() = v1
val AnimationVector2D.y : Float get() = v2

//internal class AnimatedVectorSerializer : JsonContentPolymorphicSerializer<AnimatedVector2>(
//    AnimatedVector2::class
//) {
//    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedVector2> {
//        return when(element.jsonObject["a"]?.jsonPrimitive?.int){
//            1 -> AnimatedVector2.Keyframed.serializer()
//            else -> AnimatedVector2.Default.serializer()
//        }
//    }
//}
//



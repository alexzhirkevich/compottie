package io.github.alexzhirkevich.compottie.internal.schema.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.lerp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator

internal typealias Vec2 = Offset

@OptIn(ExperimentalSerializationApi::class)
@Serializable()
@JsonClassDiscriminator("a")
internal sealed interface AnimatedVector2 : KeyframeAnimation<Vec2>, Indexable {

    @Serializable
    @SerialName("0")
    class Default(
        @SerialName("k")
        val value: FloatArray,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedVector2 {

        @Transient
        private val animationVector = Offset(value[0], value[1])

        override fun interpolated(frame: Float): Vec2 = animationVector
    }

    @Serializable
    @SerialName("1")
    class Animated(

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
    ) : AnimatedVector2, KeyframeAnimation<Vec2> by BaseKeyframeAnimation(
        keyframes = value,
        emptyValue = Offset.Zero,
        map = { s, e, p, _ ->
            Offset(
                lerp(s[0], e[0], easingX.transform(p)),
                lerp(s[1], e[1], easingY.transform(p))
            )
        }
    )
}

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



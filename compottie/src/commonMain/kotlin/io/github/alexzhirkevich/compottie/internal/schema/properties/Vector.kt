package io.github.alexzhirkevich.compottie.internal.schema.properties

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface Vector : Indexable {

    fun interpolated(time: Int): FloatArray

    @Serializable
    @SerialName("0")
    class Default(
        @SerialName("k")
        val value: FloatArray,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : Vector {

        override fun interpolated(time: Int): FloatArray = value
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
    ) : Vector {

        @Transient
        private val animation = value.to2DAnimation()

        override fun interpolated(time: Int): FloatArray {
            return animation.getValueFromNanos(
                playTimeNanos = time.milliseconds.inWholeNanoseconds,
            ).let { floatArrayOf(it.v1, it.v2) }
        }
    }
}


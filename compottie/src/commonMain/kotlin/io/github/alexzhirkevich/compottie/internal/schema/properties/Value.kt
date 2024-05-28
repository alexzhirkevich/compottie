package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.core.AnimationVector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal sealed interface Value : Indexable {

    fun interpolated(time : Int) : Float

    @Serializable
    @SerialName("0")
    class Default(
        @SerialName("k")
        val value : Float,

        @SerialName("x")
        override val expression : String? = null,

        @SerialName("ix")
        override val index : String? = null
    ) : Value {

        override fun interpolated(time: Int): Float = value
    }

    @Serializable
    @SerialName("1")
    class Keyframed(
        @SerialName("k")
        val value : List<ValueKeyframe>,

        @SerialName("x")
        override val expression : String? = null,

        @SerialName("ix")
        override val index : String? = null
    ) : Value {

        @Transient
        private val keyframes = value.toAnimation()

        override fun interpolated(time: Int): Float {
            return keyframes.getValueFromNanos(
                playTimeNanos = time.milliseconds.inWholeNanoseconds,
            ).value
        }
    }
}

private val ZeroVector = AnimationVector(0f)

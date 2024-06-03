package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class VectorKeyframe(

    @SerialName("s")
    override val start : FloatArray? = null,

    @SerialName("e")
    override val end : FloatArray? = null,

    @SerialName("t")
    override val time : Float,

    @SerialName("h")
    override val hold: BooleanInt = BooleanInt.No,

    @SerialName("i")
    override val inValue : BezierInterpolation? = null,

    @SerialName("o")
    override val outValue : BezierInterpolation? = null,

    @SerialName("ti")
    val inTangent: FloatArray? = null,

    @SerialName("to")
    val outTangent: FloatArray? = null,
) : Keyframe<FloatArray>()




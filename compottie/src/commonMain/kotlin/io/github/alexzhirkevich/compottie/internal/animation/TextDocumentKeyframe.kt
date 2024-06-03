package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class TextDocumentKeyframe(
    @SerialName("s")
    override val start: TextDocument? = null,

    @SerialName("e")
    override val end: TextDocument? = null,

    @SerialName("t")
    override val time: Float,

    //not used
    override val hold: BooleanInt = BooleanInt.No,
    override val inValue: BezierInterpolation? = null,
    override val outValue: BezierInterpolation? = null
) : Keyframe<TextDocument>()
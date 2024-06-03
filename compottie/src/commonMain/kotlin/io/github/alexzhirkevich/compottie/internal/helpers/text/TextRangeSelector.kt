package io.github.alexzhirkevich.compottie.internal.helpers.text

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class TextRangeSelector(

    @SerialName("t")
    val expressible : BooleanInt,

    @SerialName("xe")
    val maxEase : AnimatedValue? = null,

    @SerialName("ne")
    val minEase : AnimatedValue? = null,

    @SerialName("a")
    val maxAmount : AnimatedValue? = null,

    @SerialName("b")
    val basedOn : TextBased,

    @SerialName("rn")
    val randomize : BooleanInt = BooleanInt.No,

    @SerialName("sh")
    val shape : TextShape,

    @SerialName("o")
    val offset : AnimatedValue? = null,

    @SerialName("r")
    val rangeUnits : TextRangeUnits? = null,

    @SerialName("sm")
    val smoothness : AnimatedValue? = null,

    @SerialName("s")
    val start : AnimatedValue? = null,

    @SerialName("e")
    val end : AnimatedValue? = null,
)
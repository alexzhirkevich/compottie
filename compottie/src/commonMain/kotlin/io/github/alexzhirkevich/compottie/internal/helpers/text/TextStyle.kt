package io.github.alexzhirkevich.compottie.internal.helpers.text

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class TextStyle(

    @SerialName("sw")
    val strokeWidth : AnimatedValue? = null,

    @SerialName("sc")
    val strokeColor : AnimatedColor? = null,

    @SerialName("sh")
    val strokeHue : AnimatedValue? = null,

    @SerialName("ss")
    val strokeSaturation : AnimatedValue? = null,

    @SerialName("sb")
    val strokeBrightness : AnimatedValue? = null,

    @SerialName("o")
    val strokeOpacity : AnimatedValue? = null,

    @SerialName("fc")
    val fillColor : AnimatedColor? = null,

    @SerialName("fh")
    val fillHue : AnimatedValue? = null,

    @SerialName("fs")
    val fillSaturation : AnimatedValue? = null,

    @SerialName("fb")
    val fillBrightness : AnimatedValue? = null,

    @SerialName("fo")
    val fillOpacity : AnimatedValue? = null,

    @SerialName("t")
    val letterSpacing : AnimatedValue? = null,

    @SerialName("ls")
    val lineSpacing : AnimatedValue? = null,

    @SerialName("bl")
    val blur : AnimatedValue? = null,
)
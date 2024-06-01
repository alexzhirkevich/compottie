package io.github.alexzhirkevich.compottie.internal.helpers

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
internal class Mask(

    @SerialName("inv")
    val isInverted : Boolean = false,

    @SerialName("pt")
    val shape : AnimatedShape? = null,

    @SerialName("o")
    val opacity : AnimatedValue? = null,

    @SerialName("mode")
    val mode : MaskMode = MaskMode.Intersect,

    @SerialName("x")
    val expand: AnimatedValue? = null
)

@Serializable
@JvmInline
internal value class MaskMode(val type : String) {

    companion object {
        val None = MaskMode("n")
        val Add = MaskMode("a")
        val Subtract = MaskMode("s")
        val Intersect = MaskMode("i")
        val Lighten = MaskMode("l")
        val Darken = MaskMode("d")
        val Difference = MaskMode("f")
    }
}
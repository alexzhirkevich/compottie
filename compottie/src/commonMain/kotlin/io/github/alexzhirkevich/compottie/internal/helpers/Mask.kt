package io.github.alexzhirkevich.compottie.internal.helpers

import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
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
    val opacity : AnimatedNumber? = null,

    @SerialName("mode")
    val mode : MaskMode = MaskMode.Intersect,

    @SerialName("cl")
    val isClosedLegacy : Boolean? = null,

    @SerialName("x")
    val expand: AnimatedNumber? = null
) {

    init {
        // Until v 4.4.18 mask objects had a boolean cl property and c was not present in the bezier data
        if (isClosedLegacy != null) {
            shape?.setClosed(isClosedLegacy)
        }
    }

    fun deepCopy() = Mask(
        isInverted = isInverted,
        shape = shape?.copy(),
        opacity = opacity?.copy(),
        mode = mode,
        expand = expand?.copy()
    )
}

@Serializable
@JvmInline
internal value class MaskMode(val type : String) {

    init {
        if (!isSupported()){
            Compottie.logger.log("Animation contains unsupported mask type: $this. It will be treated as an 'Add' mask")
        }
    }

    override fun toString() : String {
        return when(this){
            None -> "None"
            Add -> "Add"
            Subtract -> "Subtract"
            Intersect -> "Intersect"
            Lighten -> "Lighten"
            Darken -> "Darken"
            Difference -> "Difference"
            else -> "Unknown"
        }
    }

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

internal fun MaskMode.isSupported() =
    this == MaskMode.None ||
            this == MaskMode.Add ||
            this == MaskMode.Subtract
            || this == MaskMode.Intersect
package io.github.alexzhirkevich.compottie.internal.helpers

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.animation.expressions.state
import io.github.alexzhirkevich.keight.ScriptRuntime
import io.github.alexzhirkevich.keight.js.JsAny
import io.github.alexzhirkevich.keight.js.js
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val expand: AnimatedNumber? = null,

    @SerialName("nm")
    val name: String? = null,
) : ExpressionHolder, JsAny {

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
        expand = expand?.copy(),
        name = name
    )

    override fun prepareExpressions(state: AnimationState) {
        opacity?.prepareExpressions(state)
        expand?.prepareExpressions(state)
        shape?.prepareExpressions(state)
    }

    override suspend fun get(property: JsAny?, runtime: ScriptRuntime): JsAny? {
        return when(property?.toString()){
            "invert" -> isInverted.js
            "maskOpacity" -> opacity?.raw(runtime.state)?.js
            "maskExpansion" -> expand?.raw(runtime.state)?.js
            "maskPath" -> shape
            else -> super.get(property, runtime)
        }
    }
}

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

    override fun toString() : String {
        return when(this){
            None -> "None"
            Add -> "Add"
            Subtract -> "Subtract"
            Intersect -> "Intersect"
            Lighten -> "Lighten"
            Darken -> "Darken"
            Difference -> "Difference"
            else -> "Unknown ($type)"
        }
    }
}

internal fun MaskMode.isSupported() : Boolean =
    this in supportedMasks

private val supportedMasks  = listOf(
    MaskMode.None,
    MaskMode.Add,
    MaskMode.Subtract,
    MaskMode.Intersect
)
package io.github.alexzhirkevich.compottie.internal.helpers

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
internal class StrokeDash(
    @SerialName("nm")
    val name : String? = null,

    @SerialName("mn")
    val matchName : String? = null,

    @SerialName("n")
    val dashType: DashType,

    @SerialName("v")
    val value : AnimatedNumber
) {
    fun copy() = StrokeDash(
        name = name,
        matchName = matchName,
        dashType = dashType,
        value = value.copy()
    )
}

@JvmInline
@Serializable
internal value class DashType(val type : String) {

    companion object {
        val Dash = DashType("d")
        val Gap = DashType("g")
        val Offset = DashType("o")
    }
}
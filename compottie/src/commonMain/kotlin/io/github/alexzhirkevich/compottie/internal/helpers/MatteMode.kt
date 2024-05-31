package io.github.alexzhirkevich.compottie.internal.helpers

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class MatteMode(val mode : Byte) {
    companion object {
        val Normal = MatteMode(0)
        val Alpha = MatteMode(1)
        val InvertedAlpha = MatteMode(2)
        val Luma = MatteMode(3)
        val InvertedLuma = MatteMode(4)
    }
}
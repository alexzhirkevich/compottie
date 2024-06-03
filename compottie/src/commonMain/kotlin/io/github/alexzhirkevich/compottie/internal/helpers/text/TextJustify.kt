package io.github.alexzhirkevich.compottie.internal.helpers.text

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
internal value class TextJustify(val type : Byte) {
    companion object {
        val Left = TextJustify(0)
        val Right = TextJustify(1)
        val Center = TextJustify(2)
        val LastLineLeft = TextJustify(3)
        val LastLineRight = TextJustify(4)
        val LastLineCenter = TextJustify(5)
        val LastLineFull = TextJustify(6)
    }
}

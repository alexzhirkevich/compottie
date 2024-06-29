package io.github.alexzhirkevich.compottie.internal.helpers.text

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class TextJustify internal constructor(val type : Byte) {

    companion object {
        val Left = TextJustify(0)
        val Right = TextJustify(1)
        val Center = TextJustify(2)
        internal val LastLineLeft = TextJustify(3)
        internal val LastLineRight = TextJustify(4)
        internal val LastLineCenter = TextJustify(5)
        internal val LastLineFull = TextJustify(6)
    }
}

package io.github.alexzhirkevich.compottie.internal.platform

internal actual fun CharSequence.codePointAt(index : Int) =
    Character.codePointAt(this, index)


internal actual fun charCount(codePoint : Int): Int = Character.charCount(codePoint)


private val modifierSet = setOf(
    Character.FORMAT,
    Character.MODIFIER_SYMBOL,
    Character.NON_SPACING_MARK,
    Character.OTHER_SYMBOL,
    Character.DIRECTIONALITY_NONSPACING_MARK,
    Character.SURROGATE,
)

internal actual fun isModifier(codePoint : Int): Boolean {
    return Character.getType(codePoint).toByte() in modifierSet
}

internal actual fun StringBuilder.addCodePoint(codePoint : Int) {
    this.appendCodePoint(codePoint)
}




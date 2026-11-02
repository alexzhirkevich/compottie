package io.github.alexzhirkevich.compottie.internal.platform

import androidx.compose.ui.util.fastAny

internal expect fun CharSequence.codePointAt(index : Int) : Int

internal expect fun charCount(codePoint : Int): Int

internal expect fun StringBuilder.addCodePoint(codePoint : Int)

private val modifierSet = listOf(
    CharCategory.FORMAT,
    CharCategory.MODIFIER_SYMBOL,
    CharCategory.NON_SPACING_MARK,
    CharCategory.OTHER_SYMBOL,
    CharCategory.SURROGATE,
    CharCategory.COMBINING_SPACING_MARK,
)

internal fun isModifier(codePoint : Int): Boolean {
    if (codePoint > 0xFFFF) {
        return codePoint in 0x1F3FB..0x1F3FF || // skin tone modifiers
                codePoint in 0xE0020..0xE007F || // tag characters (subdivision flags)
                codePoint in 0xE0100..0xE01EF // variation selectors supplement
    }
    return codePoint == 0x20E3 || // keycap
            modifierSet.fastAny { it.contains(codePoint.toChar()) }
}

internal fun isRegionalIndicator(codePoint: Int) = codePoint in 0x1F1E6..0x1F1FF

/**
 * Indic viramas (pulli in Tamil). A consonant that follows a virama is part of
 * the same syllable and can form a conjunct with it (க்ஷ, ஸ்ரீ).
 */
internal fun isIndicVirama(codePoint: Int): Boolean = when (codePoint) {
    0x094D, 0x09CD, 0x0A4D, 0x0ACD, 0x0B4D, 0x0BCD, 0x0C4D, 0x0CCD, 0x0D4D,
    0x0DCA, 0x1039, 0x17D2, 0xA9C0 -> true
    else -> false
}

internal fun isLetterCodePoint(codePoint: Int): Boolean =
    codePoint < 0x10000 && codePoint.toChar().isLetter()
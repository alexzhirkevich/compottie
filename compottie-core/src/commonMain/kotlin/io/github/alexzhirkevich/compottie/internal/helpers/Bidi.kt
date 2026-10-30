package io.github.alexzhirkevich.compottie.internal.helpers

import io.github.alexzhirkevich.compottie.internal.platform.charCount
import io.github.alexzhirkevich.compottie.internal.platform.codePointAt

/**
 * Lightweight, multiplatform stand-ins for the pieces of `java.text.Bidi` and
 * `Character.getDirectionality` that lottie-android's TextLayer relies on for RTL text.
 *
 * This is an approximation of the Unicode Bidirectional Algorithm:
 *  - the paragraph (line) direction is taken from the first strong character, like
 *    Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT (LTR only when there is no strong character)
 *  - strong RTL (Hebrew, Arabic, Syriac, Thaana, ...) and LTR characters get embedding levels
 *  - digits are laid out left-to-right inside RTL text
 *  - neutrals (spaces, punctuation) between same-direction text take that direction,
 *    otherwise the paragraph direction
 *  - visual order is produced with the standard "reverse from highest level" step (rule L2)
 *  - bracket mirroring and explicit embedding/override controls are not handled
 *
 * If exact UBA behaviour is needed, replace [requiresBidi] and the reordering step in
 * TextLayer with an expect/actual wrapper over java.text.Bidi / ICU on the platforms that have it.
 */
internal enum class BidiClass { LTR, RTL, DIGIT, NEUTRAL }

internal fun bidiClassOf(codePoint: Int): BidiClass {
    if (isDigitCodePoint(codePoint)) {
        return BidiClass.DIGIT
    }
    if (isRightToLeftCodePoint(codePoint)) {
        return BidiClass.RTL
    }
    return if (codePoint < 0x10000 && codePoint.toChar().isLetter()) {
        BidiClass.LTR
    } else {
        BidiClass.NEUTRAL
    }
}

/**
 * Whether the text contains any strong right-to-left character, i.e. whether
 * it has to be reordered visually before being drawn character by character.
 */
internal fun requiresBidi(text: String): Boolean {
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        if (bidiClassOf(cp) == BidiClass.RTL) {
            return true
        }
        i += charCount(cp)
    }
    return false
}

/**
 * Whether the string contains a joining (connecting, such as ت + ت should become تت) character
 * that is written from right to left. Such characters must be drawn together so the text
 * shaper can pick the correct joining glyph.
 *
 * Equivalent to Character.getDirectionality(cp) == DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC.
 */
internal fun isJoiningRightToLeft(stringChar: String): Boolean {
    var i = 0
    while (i < stringChar.length) {
        val cp = stringChar.codePointAt(i)
        if (isArabicLetterCodePoint(cp)) {
            return true
        }
        i += charCount(cp)
    }
    return false
}

private fun isDigitCodePoint(cp: Int): Boolean =
    cp in 0x0030..0x0039 || // ASCII
            cp in 0x0660..0x0669 || // Arabic-Indic
            cp in 0x06F0..0x06F9 // Extended Arabic-Indic

private fun isArabicLetterCodePoint(cp: Int): Boolean {
    if (isDigitCodePoint(cp)) {
        return false
    }
    // Combining marks are not letters (they are clustered with their base char anyway)
    if (cp in 0x064B..0x065F || cp == 0x0670) {
        return false
    }
    return cp in 0x0600..0x07BF || // Arabic, Syriac, Arabic Supplement, Thaana
            cp in 0x08A0..0x08FF || // Arabic Extended-A
            cp in 0xFB50..0xFDFF || // Arabic Presentation Forms-A
            cp in 0xFE70..0xFEFF // Arabic Presentation Forms-B
}

private fun isRightToLeftCodePoint(cp: Int): Boolean =
    cp in 0x0590..0x08FF || // Hebrew, Arabic, Syriac, Thaana, NKo, Samaritan, Mandaic, ...
            cp in 0xFB1D..0xFDFF || // Hebrew & Arabic presentation forms A
            cp in 0xFE70..0xFEFF || // Arabic presentation forms B
            cp in 0x10800..0x10FFF || // RTL historic scripts
            cp in 0x1E800..0x1EFFF // Mende Kikakui, Adlam, Arabic mathematical, ...
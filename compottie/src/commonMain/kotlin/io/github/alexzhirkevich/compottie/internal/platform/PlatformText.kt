package io.github.alexzhirkevich.compottie.internal.platform

internal expect fun CharSequence.codePointAt(index : Int) : Int

internal expect fun charCount(codePoint : Int): Int

internal expect fun isModifier(codePoint : Int): Boolean

internal expect fun StringBuilder.addCodePoint(codePoint : Int)

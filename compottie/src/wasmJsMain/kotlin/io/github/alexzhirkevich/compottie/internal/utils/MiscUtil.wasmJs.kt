package io.github.alexzhirkevich.compottie.internal.utils

internal actual fun currentTimeMs(): Long = js("Date.now()")
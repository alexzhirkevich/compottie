package io.github.alexzhirkevich.compottie

internal actual fun currentTime(): Long = currentTimeDouble().toLong()

private fun currentTimeDouble(): Double = js("Date.now()")
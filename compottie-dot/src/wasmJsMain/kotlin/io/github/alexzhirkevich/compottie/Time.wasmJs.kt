package io.github.alexzhirkevich.compottie

internal actual fun currentTime(): Long = currentTimeDouble().toLong()

private fun currentTimeDouble(): Number = js("Date.now()")
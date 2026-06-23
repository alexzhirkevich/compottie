package io.github.alexzhirkevich.compottie

internal actual fun currentTime(): Long = currentTimeDouble().toLong()

@OptIn(ExperimentalWasmJsInterop::class)
private fun currentTimeDouble(): Double = js("Date.now()")

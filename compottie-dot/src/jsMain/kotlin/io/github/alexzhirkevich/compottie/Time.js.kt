package io.github.alexzhirkevich.compottie

import kotlin.js.Date

internal actual fun currentTime(): Long {
    return Date.now().toLong()
}
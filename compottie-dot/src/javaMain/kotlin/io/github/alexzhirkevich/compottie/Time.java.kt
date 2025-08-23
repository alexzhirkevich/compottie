package io.github.alexzhirkevich.compottie

internal actual fun currentTime(): Long {
    return java.time.Clock.systemUTC().millis()
}
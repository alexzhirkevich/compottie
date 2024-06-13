package io.github.alexzhirkevich.compottie

class CompottieException internal constructor(
    msg : String,
    cause : Throwable? = null
) : Exception(msg,cause) {
}
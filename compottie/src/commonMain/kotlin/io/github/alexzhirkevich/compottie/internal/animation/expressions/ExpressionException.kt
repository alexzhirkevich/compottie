package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.CompottieException
import io.github.alexzhirkevich.compottie.InternalCompottieApi

@OptIn(InternalCompottieApi::class)
class ExpressionException(
    msg : String,
    cause : Throwable? = null
) : CompottieException(msg, cause)
package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


/**
 * Specification for a [LottieComposition]. Each subclass represents a different source.
 * A [LottieComposition] is the stateless parsed version of a Lottie json file and is
 * passed into [rememberLottieComposition] or [LottieAnimation].
 */
@Immutable
expect sealed class LottieCompositionSpec {

    /**
     * Load an animation from its json string.
     */
    @Immutable
    class JsonString(jsonString: String) : LottieCompositionSpec

    companion object
}

//internal expect fun LottieCompositionSpec.JsonString()



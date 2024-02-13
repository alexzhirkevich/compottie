package io.github.alexzhirkevich.compottie

import com.airbnb.lottie.compose.LottieCompositionSpec as PlatformLottieCompositionSpec
/**
 * Specification for a [com.airbnb.lottie.LottieComposition]. Each subclass represents a different source.
 * A [com.airbnb.lottie.LottieComposition] is the stateless parsed version of a Lottie json file and is
 * passed into [rememberLottieComposition] or [LottieAnimation].
 */

actual sealed class LottieCompositionSpec(
    internal val delegate : com.airbnb.lottie.compose.LottieCompositionSpec
) {

    actual class JsonString actual constructor(jsonString: String) : LottieCompositionSpec(
        PlatformLottieCompositionSpec.JsonString(jsonString)
    )

    actual class Url actual constructor(url: String) : LottieCompositionSpec(
        PlatformLottieCompositionSpec.Url(url)
    )

    actual companion object {}
}


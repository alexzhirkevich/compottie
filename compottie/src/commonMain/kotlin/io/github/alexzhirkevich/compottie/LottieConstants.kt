package io.github.alexzhirkevich.compottie


@Deprecated(
    "Use Compottie accessor instead",
    replaceWith = ReplaceWith(
        "Compottie",
        "io.github.alexzhirkevich.compottie.Compottie"
    )
)
object LottieConstants {
    /**
     * Use this with [animateLottieCompositionAsState]'s iterations parameter to repeat forever.
     */
    const val IterateForever = Int.MAX_VALUE
}
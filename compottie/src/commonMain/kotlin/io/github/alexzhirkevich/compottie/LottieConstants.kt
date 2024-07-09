package io.github.alexzhirkevich.compottie


@Deprecated(
    "Use Compottie accessor instead",
    replaceWith = ReplaceWith(
        "Compottie",
        "io.github.alexzhirkevich.compottie.Compottie"
    )
)
public object LottieConstants {
    /**
     * Use this with [animateLottieCompositionAsState]'s iterations parameter to repeat forever.
     */
    public const val IterateForever: Int = Int.MAX_VALUE
}
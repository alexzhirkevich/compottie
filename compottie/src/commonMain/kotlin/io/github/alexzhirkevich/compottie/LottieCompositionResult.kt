package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

/**
 * A [LottieCompositionResult] subclass is returned from [rememberLottieComposition].
 * It can be completed with a result or exception only one time.
 *
 * This class implements State<LottieComposition> so you either use it like:
 * ```
 * val compositionResult = rememberLottieComposition(...)
 * // Or
 * val composition by rememberLottieComposition(...)
 * ```
 *
 * Use the former if you need to explicitly differentiate between loading and error states
 * or if you need to call [await] or [awaitOrNull] in a coroutine such as [androidx.compose.runtime.LaunchedEffect].
 *
 * @see rememberLottieComposition
 * @see LottieAnimation
 */
@Stable
expect interface LottieCompositionResult : State<LottieComposition?> {
    /**
     * The composition or null if it hasn't yet loaded or failed to load.
     */
    override val value: LottieComposition?

    /**
     * The exception that was thrown while trying to load and parse the composition.
     */
    val error: Throwable?

    /**
     * Whether or not the composition is still being loaded and parsed.
     */
    val isLoading: Boolean

    /**
     * Whether or not the composition is in the process of being loaded or parsed.
     */
    val isComplete: Boolean

    /**
     * Whether or not the composition failed to load. This is terminal. It only occurs after
     * returning false from [rememberLottieComposition]'s onRetry lambda.
     */
    val isFailure: Boolean

    /**
     * Whether or not the composition has succeeded yet.
     */
    val isSuccess: Boolean

    /**
     * Suspend until the composition has finished parsing.
     *
     * This can throw if the [LottieComposition] fails to load.
     *
     * These animations should never fail given a valid input:
     * * [LottieCompositionSpec.RawRes]
     * * [LottieCompositionSpec.Asset]
     * * [LottieCompositionSpec.JsonString]
     *
     * These animations may fail:
     * * [LottieCompositionSpec.Url]
     * * [LottieCompositionSpec.File]
     */
    suspend fun await(): LottieComposition
}

/**
 * Like [LottieCompositionResult.await] but returns null instead of throwing an exception if the animation fails
 * to load.
 */
suspend fun LottieCompositionResult.awaitOrNull(): LottieComposition? {
    return try {
        await()
    } catch (e: Throwable) {
        null
    }
}


package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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
public interface LottieCompositionResult : State<LottieComposition?> {
    /**
     * The composition or null if it hasn't yet loaded or failed to load.
     */
    override val value: LottieComposition?

    /**
     * The exception that was thrown while trying to load and parse the composition.
     */
    public val error: Throwable?

    /**
     * Whether or not the composition is still being loaded and parsed.
     */
    public val isLoading: Boolean

    /**
     * Whether or not the composition is in the process of being loaded or parsed.
     */
    public val isComplete: Boolean

    /**
     * Whether or not the composition failed to load. This is terminal. It only occurs after
     * returning false from [rememberLottieComposition]'s onRetry lambda.
     */
    public val isFailure: Boolean

    /**
     * Whether or not the composition has succeeded yet.
     */
    public val isSuccess: Boolean

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
    public suspend fun await(): LottieComposition
}


@Stable
internal class LottieCompositionResultImpl() : LottieCompositionResult {

    private var compositionDeferred = CompletableDeferred<LottieComposition>()

    override var value: LottieComposition? by mutableStateOf(null)
        private set

    override var error by mutableStateOf<Throwable?>(null)
        private set

    override val isLoading by derivedStateOf { value == null && error == null }

    override val isComplete by derivedStateOf { value != null || error != null }

    override val isFailure by derivedStateOf { error != null }

    override val isSuccess by derivedStateOf { value != null }

    override suspend fun await(): LottieComposition {
        return compositionDeferred.await()
    }

    private val mutex = Mutex()

    // MAIN THREAD!!!
    internal suspend fun complete(composition: LottieComposition) {
        mutex.withLock {
            if (isComplete) return@withLock

            value = composition
            compositionDeferred.complete(composition)
        }
    }

    // MAIN THREAD!!!
    internal suspend fun completeExceptionally(error: Throwable) {
        mutex.withLock {
            if (isComplete) return@withLock

            this.error = error
            compositionDeferred.completeExceptionally(error)
        }
    }
}

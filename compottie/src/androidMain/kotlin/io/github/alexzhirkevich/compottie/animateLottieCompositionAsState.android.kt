package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.LottieAnimationState as PlatformLottieAnimationState

@Composable
@Stable
actual fun animateLottieCompositionAsState(
    composition: LottieComposition?,
    isPlaying: Boolean,
    restartOnPlay: Boolean,
    reverseOnRepeat: Boolean,
    clipSpec: LottieClipSpec?,
    speed: Float,
    iterations: Int,
    cancellationBehavior: LottieCancellationBehavior,
    ignoreSystemAnimatorScale: Boolean,
    useCompositionFrameRate: Boolean,
): LottieAnimationState {

    val platform = animateLottieCompositionAsState(
        composition,
        isPlaying,
        restartOnPlay,
        reverseOnRepeat,
        clipSpec?.delegate,
        speed,
        iterations,
        cancellationBehavior,
        ignoreSystemAnimatorScale,
        useCompositionFrameRate
    )

    return remember(platform) {
        DelegatedLottieAnimationState(platform)
    }
}


@Stable
private class DelegatedLottieAnimationState(
    private val delegate : PlatformLottieAnimationState
) : LottieAnimationState {
    override val isPlaying: Boolean
        get() = delegate.isPlaying
    override val progress: Float
        get() = delegate.progress
    override val iteration: Int
        get() = delegate.iteration
    override val iterations: Int
        get() = delegate.iterations
    override val reverseOnRepeat: Boolean
        get() = delegate.reverseOnRepeat

    override val clipSpec: LottieClipSpec? =
        delegate.clipSpec?.let(::DelegatedLottieClipSpec)
    override val speed: Float
        get() = delegate.speed
    override val useCompositionFrameRate: Boolean
        get() = delegate.useCompositionFrameRate
    override val composition: LottieComposition?
        get() = delegate.composition
    override val lastFrameNanos: Long
        get() = delegate.lastFrameNanos
    override val isAtEnd: Boolean
        get() = delegate.isAtEnd
    override val value: Float
        get() = delegate.value

}

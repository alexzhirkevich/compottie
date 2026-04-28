package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.LottieAnimatable
import io.github.alexzhirkevich.compottie.LottieStateMachine
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Stable
internal sealed interface SMTransition {

    public val toState: String
    public val guards: List<SMGuard>

    public suspend fun move(
        state: AnimationState,
        progress: LottieAnimatable,
        toProgress: Float
    )

    public fun canMove(machine: LottieStateMachine): Boolean {
        return guards.fastAll { it.check(machine) }
    }

    @Serializable
    @SerialName("Transition")
    public class Default(
        public override val toState: String,
        public override val guards: List<SMGuard> = emptyList()
    ) : SMTransition {

        override suspend fun move(
            state: AnimationState,
            progress: LottieAnimatable,
            toProgress: Float
        ) {
            progress.snapTo(state.composition, toProgress)
        }
    }

    @Serializable
    @SerialName("Tweened")
    public class Tweened(
        public override val toState: String,
        public val duration: Float,
        public val easing: List<Float>,
        public override val guards: List<SMGuard> = emptyList()
    ) : SMTransition {

        @Transient
        private val animationSpec = tween<Float>(
            durationMillis = (duration * 1000).toInt(),
            easing = runCatching {
                CubicBezierEasing(
                    easing[0],
                    easing[1],
                    easing[2],
                    easing[3],
                )
            }.getOrDefault(LinearEasing)
        )

        override suspend fun move(
            state: AnimationState,
            progress: LottieAnimatable,
            toProgress: Float
        ) {
            state.tweenTo(
                frame = state.composition.progressToFrame(toProgress),
                spec = animationSpec
            ) {
                progress.snapTo(
                    composition = state.composition,
                    progress = lerp(
                        start = state.progress,
                        stop = toProgress,
                        fraction = state.tweenProgress
                    )
                )
            }
        }
    }
}
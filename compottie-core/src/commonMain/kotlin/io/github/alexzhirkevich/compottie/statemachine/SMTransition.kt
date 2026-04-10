package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastAll
import io.github.alexzhirkevich.compottie.LottieAnimatable
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieStateMachine
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Stable
internal sealed interface SMTransition {

    public val toState: String
    public val guards: List<SMGuard>

    public suspend fun move(
        composition: LottieComposition,
        progress: LottieAnimatable,
        toProgress: Float
    ) {
        progress.snapTo(composition, toProgress)
    }

    public fun canMove(machine: LottieStateMachine): Boolean {
        return guards.fastAll { it.check(machine) }
    }

    @Serializable
    @SerialName("Transition")
    public class Default(
        public override val toState: String,
        public override val guards: List<SMGuard> = emptyList()
    ) : SMTransition

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

        @Transient
        private val animatable = Animatable(0f)

        override suspend fun move(
            composition: LottieComposition,
            progress: LottieAnimatable,
            toProgress: Float
        ) {
            progress.snapTo(composition, progress.progress)
            animatable.snapTo(progress.progress)
            animatable.animateTo(
                targetValue = toProgress,
                animationSpec = animationSpec
            ) {
                progress.updateProgress(composition, value)
            }
        }
    }
}
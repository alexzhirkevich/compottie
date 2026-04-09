package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastAll
import io.github.alexzhirkevich.compottie.LottieAnimatable
import io.github.alexzhirkevich.compottie.LottieComposition
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Stable
internal sealed interface SMTransition {

    public val toState: String
    public val guards: List<SMGuard>?

    public suspend fun move(
        composition: LottieComposition,
        progress: LottieAnimatable,
        toProgress: Float
    ) {

    }

    public fun canMove(variables : Map<String, Any>): Boolean {
        return guards.orEmpty().fastAll { it.check(variables) }
    }

    @Serializable
    @SerialName("Transition")
    public class Default(
        public override val toState: String,
        public override val guards: List<SMGuard>? = null
    ) : SMTransition

    @Serializable
    @SerialName("Tweened")
    public class Tweened(
        public override val toState: String,
        public val duration: Float,
        public val easing: List<Float>,
        public override val guards: List<SMGuard>? = null
    ) : SMTransition {

        @Transient
        private val animationSpec = tween<Float>(
            durationMillis = (duration * 1000).toInt(),
            easing = CubicBezierEasing(
                easing[0],
                easing[1],
                easing[2],
                easing[3],
            )
        )

        override suspend fun move(
            composition: LottieComposition,
            progress: LottieAnimatable,
            toProgress: Float
        ) {
            coroutineScope {
                Animatable(progress.progress).animateTo(
                    targetValue = toProgress,
                    animationSpec = animationSpec
                ) {
                    launch {
                        progress.snapTo(composition, progress = value)
                    }
                }
            }
        }
    }
}
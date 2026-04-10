package io.github.alexzhirkevich.compottie.statemachine

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieAnimatable
import io.github.alexzhirkevich.compottie.LottieClipSpec
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal sealed interface SMState {

    public val name : String
    public val transitions : List<SMTransition>
    public val entryActions : List<SMAction>
    public val exitActions : List<SMAction>

    public val sortedTransitions : List<SMTransition>

    public val modifier: Modifier get() = Modifier

    public suspend fun move(
        state : AnimationState,
        progress : LottieAnimatable,
        transition : SMTransition
    ) {

    }

    public suspend fun play(
        composition: LottieComposition,
        progress : LottieAnimatable,
    ){

    }

    @Serializable
    @SerialName("PlaybackState")
    public class PlaybackState(
        public override val name : String,
        public override val transitions : List<SMTransition>,
        public val animation : String,
        public val loop : Boolean = false,
        public val loopCount : Int = 1,
        public val autoplay : Boolean = false,
        public val final : Boolean = false,
        public val mode : SMPlaybackMode = SMPlaybackMode.Forward,
        public val speed : Float = 1f,
        public val segment : String? = null,
        public val backgroundColor : String? = null,
        override val entryActions: List<SMAction> = emptyList(),
        override val exitActions: List<SMAction> = emptyList(),
    ) : SMState {

        @Transient
        private val bgColor: Color? = backgroundColor?.let {
            it.substringAfter(it.lowercase().substringAfter("0x"))
                .toLongOrNull(16)
                ?.let { Color(it) }
        }

        override val modifier: Modifier
            get() = bgColor?.let { Modifier.background(it) } ?: Modifier

        @Transient
        override val sortedTransitions: List<SMTransition> = transitions.sortedBy {
            if (it.guards == null) 1 else 0
        }

        override suspend fun move(
            state: AnimationState,
            progress: LottieAnimatable,
            transition: SMTransition
        ) {
            val start = state.composition.marker(segment)?.let {
                state.composition.frameToProgress(
                    if (mode.isReverse)
                        it.startFrame + it.durationFrames
                    else it.startFrame
                )
            }

            if (start != null) {
                transition.move(state.composition, progress, start)
            }
        }

        override suspend fun play(
            composition: LottieComposition,
            progress: LottieAnimatable
        ) {
            if (autoplay) {
                progress.animate(
                    initialProgress = progress.progress,
                    composition = composition,
                    iterations = if (loop) Compottie.IterateForever else loopCount,
                    clipSpec = if (segment != null && composition.marker(segment) != null)
                        LottieClipSpec.Marker(segment)
                    else null,
                    reverseOnRepeat = mode.isBounce,
                    speed = speed * if (mode.isReverse) -1f else 1f,
                )
            }
        }
    }

    @Serializable
    @SerialName("GlobalState")
    public class GlobalState(
        public override val name : String,
        public override val transitions : List<SMTransition>,
        override val entryActions: List<SMAction> = emptyList(),
        override val exitActions: List<SMAction> = emptyList(),
    ) : SMState {

        @Transient
        override val sortedTransitions: List<SMTransition> = transitions.sortedBy {
            if (it.guards == null) 1 else 0
        }

    }
}
package io.github.alexzhirkevich.compottie.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class AnimationState @PublishedApi internal constructor(
    frame : Float,
    val composition : LottieComposition,
    fontFamilyResolver: FontFamily.Resolver,
    clipToDrawBounds : Boolean = true,
    dynamicProperties : DynamicCompositionProvider? = null,
    clipTextToBoundingBoxes : Boolean = false,
) {
    internal var clipToCompositionBounds by mutableStateOf(clipToDrawBounds)
    internal var clipTextToBoundingBoxes by mutableStateOf(clipTextToBoundingBoxes)
    internal var fontFamilyResolver by mutableStateOf(fontFamilyResolver)
    internal var dynamic by mutableStateOf(dynamicProperties)

    var frame = frame
        private set

    val progress: Float
        get() {
            val p = (frame - composition.animation.inPoint) /
                    (composition.animation.outPoint - composition.animation.inPoint)
            return p.coerceIn(0f, 1f)
        }

    @PublishedApi
    internal fun setFrame(frame: Float) {
        this.frame = frame
    }

    /**
     * Remaps current state to requested [frame] and performs [block] on it.
     * State is restored after the [block] call
     * */
    @OptIn(ExperimentalContracts::class)
    fun <R> remapped(frame: Float, block: (AnimationState) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val initial = this.frame

        return try {
            setFrame(frame)
            block(this)
        } finally {
            setFrame(initial)
        }
    }
}

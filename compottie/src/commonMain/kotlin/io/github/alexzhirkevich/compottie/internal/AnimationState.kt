package io.github.alexzhirkevich.compottie.internal

import io.github.alexzhirkevich.compottie.LottieComposition
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

class AnimationState @PublishedApi internal constructor(
    frame : Float,
    val composition : LottieComposition
) {
    var frame = frame
        private set

    val progress : Float
        get() {
            val p = (frame - composition.lottieData.inPoint)/
                    (composition.lottieData.outPoint - composition.lottieData.inPoint)
            return p.coerceIn(0f,1f)
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

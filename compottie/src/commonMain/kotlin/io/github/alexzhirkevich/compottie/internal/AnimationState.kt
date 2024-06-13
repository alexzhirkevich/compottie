package io.github.alexzhirkevich.compottie.internal

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
value class AnimationState @PublishedApi internal constructor(val frame : Float) {

    /**
     * Remaps current state to requested [frame] and performs [block] on it.
     * State is restored after the [block] call
     * */
    @OptIn(ExperimentalContracts::class)
    inline fun <R> remapped(frame: Float, block : (AnimationState) -> R) : R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return block(AnimationState(frame))
    }
}

//internal interface AnimationState {
//
//    val animation : LottieData
//
//    val frame: Float
//}
//
//internal class CompositionAnimationState(
//    private val composition: LottieComposition,
//    private val progressProvider : () -> Float,
//) : AnimationState {
//
//    override val animation: LottieData
//        get() = composition.lottieData
//
//    override val frame: Float by derivedStateOf {
//        val p = composition.lottieData.outPoint * progressProvider() -
//                composition.lottieData.inPoint
//        p.coerceAtLeast(0f)
//    }
//}
//
//internal class RemappedAnimationState(
//    private val frameRemapping : (AnimationState) -> Float = { it.frame },
//) : AnimationState {
//
//    var delegate: AnimationState? = null
//
//    override val frame: Float by derivedStateOf {
//        frameRemapping(checkNotNull(delegate))
//    }
//
//    override val animation: LottieData
//        get() = checkNotNull(delegate).animation
//}
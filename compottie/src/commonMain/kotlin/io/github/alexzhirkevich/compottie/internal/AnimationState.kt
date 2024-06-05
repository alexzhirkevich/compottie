package io.github.alexzhirkevich.compottie.internal

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.github.alexzhirkevich.compottie.LottieComposition
import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline

@JvmInline
value class AnimationState(val frame : Float)

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
package io.github.alexzhirkevich.compottie.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.dynamic.DynamicCompositionProvider
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class AnimationState @PublishedApi internal constructor(
    val composition : LottieComposition,
    internal val assets: Map<String, LottieAsset>,
    internal val fonts : Map<String, FontFamily>,
    frame : Float,
    fontFamilyResolver: FontFamily.Resolver,
    clipToDrawBounds : Boolean,
    dynamicProperties : DynamicCompositionProvider?,
    clipTextToBoundingBoxes : Boolean,
    enableMergePaths: Boolean,
    layer : Layer
) {
    var frame by mutableStateOf(frame)
        private set

    val progress: Float
        get() {
            val p = (frame - composition.animation.inPoint) /
                    (composition.animation.outPoint - composition.animation.inPoint)
            return p.coerceIn(0f, 1f)
        }

    internal var clipToCompositionBounds by mutableStateOf(clipToDrawBounds)
    internal var clipTextToBoundingBoxes by mutableStateOf(clipTextToBoundingBoxes)
    internal var fontFamilyResolver by mutableStateOf(fontFamilyResolver)
    internal var dynamic by mutableStateOf(dynamicProperties)
    internal var enableMergePaths by mutableStateOf(enableMergePaths)
    internal var layer by mutableStateOf(layer)
        private set

    /**
     * Remaps current state to requested [frame] and performs [block] on it.
     * State is restored after the [block] call
     * */
    @OptIn(ExperimentalContracts::class)
    internal fun <R> onFrame(frame: Float, block: (AnimationState) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val initial = this.frame

        return try {
            this.frame = frame
            block(this)
        } finally {
            this.frame = initial
        }
    }

    @OptIn(ExperimentalContracts::class)
    internal fun <R> onLayer(layer: Layer, block: (AnimationState) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        val prevLayer = this.layer
        return try {
            this.layer = layer
            block(this)
        } finally {
            this.layer = prevLayer
        }
    }
}

package io.github.alexzhirkevich.compottie.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class AnimationState @PublishedApi internal constructor(
    val composition: LottieComposition,
    internal val assets: Map<String, LottieAsset>,
    internal val fonts: Map<String, FontFamily>,
    frame: Float,
    fontFamilyResolver: FontFamily.Resolver,
    applyOpacityToLayers : Boolean,
    clipToCompositionBounds: Boolean,
    clipTextToBoundingBoxes: Boolean,
    enableMergePaths: Boolean,
    layer: Layer
) {
    /**
     * All successfully loaded images for this animation by the asset id
     * */
    val images : Map<String, ImageBitmap> = assets
        .filterValues { it is ImageAsset && it.bitmap != null }
        .mapValues { (it.value as ImageAsset).bitmap!! }

    var frame = frame
        private set

    val progress: Float
        get() {
            val p = (frame - composition.animation.inPoint) /
                    (composition.animation.outPoint - composition.animation.inPoint)
            return p.coerceIn(0f, 1f)
        }

    internal var clipToCompositionBounds by mutableStateOf(clipToCompositionBounds)
    internal var clipTextToBoundingBoxes by mutableStateOf(clipTextToBoundingBoxes)
    internal var fontFamilyResolver by mutableStateOf(fontFamilyResolver)
    internal var enableMergePaths by mutableStateOf(enableMergePaths)
    internal var applyOpacityToLayers by mutableStateOf(applyOpacityToLayers)

    internal var layer = layer
        private set

    /**
     * Remaps current state to requested [frame] and performs [block] on it.
     * State is restored after the [block] call
     * */
    @OptIn(ExperimentalContracts::class)
    internal inline fun <R> onFrame(frame: Float, block: (AnimationState) -> R): R {
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
    internal inline fun <R> onLayer(layer: Layer, block: (AnimationState) -> R): R {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AnimationState

        if (composition != other.composition) return false
        if (assets != other.assets) return false
        if (fonts != other.fonts) return false
        if (frame != other.frame) return false
        if (clipToCompositionBounds != other.clipToCompositionBounds) return false
        if (clipTextToBoundingBoxes != other.clipTextToBoundingBoxes) return false
        if (fontFamilyResolver != other.fontFamilyResolver) return false
        if (enableMergePaths != other.enableMergePaths) return false

        return true
    }

    override fun hashCode(): Int {
        var result = composition.hashCode()
        result = 31 * result + assets.hashCode()
        result = 31 * result + fonts.hashCode()
        result = 31 * result + frame.hashCode()
        result = 31 * result + clipToCompositionBounds.hashCode()
        result = 31 * result + clipTextToBoundingBoxes.hashCode()
        result = 31 * result + fontFamilyResolver.hashCode()
        result = 31 * result + enableMergePaths.hashCode()
        return result
    }


}

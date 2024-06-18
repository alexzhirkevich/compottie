package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.graphics.ImageBitmap
import io.github.alexzhirkevich.compottie.internal.AnimationState

@PublishedApi
internal class DynamicImageLayerProvider() : DynamicLayerProvider(), DynamicImageLayer {

    var image: (AnimationState.(source: ImageSpec?) -> ImageBitmap)? = null
        private set

    override fun image(image: AnimationState.(source: ImageSpec?) -> ImageBitmap) {
        this.image = image
    }
}

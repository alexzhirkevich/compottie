package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.graphics.ImageBitmap
import io.github.alexzhirkevich.compottie.internal.AnimationState

interface DynamicImageLayer: DynamicLayer {

    /**
     * Configure dynamic image.
     *
     * Image must be exact same size as required in spec (if spec is not null)
     */
    fun image(image: AnimationState.(spec : ImageSpec?) -> ImageBitmap)
}

data class ImageSpec(
    val id : String,
    val path : String,
    val name : String,
    val width : Int,
    val height : Int
)
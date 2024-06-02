package io.github.alexzhirkevich.compottie.internal.layers

import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.Transform

internal class CompositionLayer(
    composition: LottieComposition
) : BaseCompositionLayer() {

    init {
        this.composition = composition
    }

    override val width: Float get() = composition.lottieData.width
    override val height: Float get() = composition.lottieData.height
    override val timeRemapping: AnimatedValue? get() = null

    override val masks: List<Mask>? get() = null
    override val transform: Transform = Transform()

    override val is3d: BooleanInt get() = BooleanInt.No
    override val hidden: Boolean get() = false
    override val index: Int? get() = null
    override val parent: Int? get() = null
    override val timeStretch: Float get() = 1f

    override val inPoint: Float get() = composition.lottieData.inPoint
    override val outPoint: Float get() = composition.lottieData.outPoint
    override val startTime: Int get() = 0
    override val name: String? = null

    override fun loadLayers(): List<Layer> {
        return composition.lottieData.layers
    }
}
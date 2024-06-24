package io.github.alexzhirkevich.compottie.internal.layers

import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform

internal class CompositionLayer(
    private val composition: LottieComposition
) : BaseCompositionLayer() {

    override val width: Float get() = composition.animation.width
    override val height: Float get() = composition.animation.height
    override val timeRemapping: AnimatedNumber? get() = null

    override var resolvingPath: ResolvingPath? = ResolvingPath.root

    override val masks: List<Mask>? get() = null

    override val hasMask: Boolean get() = false

    override var effects: List<LayerEffect> = emptyList()

    override val transform: Transform = Transform()
    override val autoOrient: BooleanInt get() = BooleanInt.No
    override val matteMode: MatteMode? get() = null
    override val matteParent: Int? get() = null
    override val matteTarget: BooleanInt? get() = null
    override val clazz: String? get() = null
    override val htmlId: String? get() = null
    override val collapseTransform: BooleanInt get() = BooleanInt.No

    override val is3d: BooleanInt get() = BooleanInt.No
    override val hidden: Boolean get() = false
    override val index: Int? get() = null
    override val parent: Int? get() = null
    override val timeStretch: Float get() = 1f

    override val blendMode: LottieBlendMode get() = LottieBlendMode.Normal

    override val inPoint: Float? get() = null

    override val outPoint: Float? get() = null

    override val startTime: Float? get() = null

    override val name: String = CONTAINER_NAME

    override fun compose(state: AnimationState): List<Layer> {
        return composition.animation.layers
    }

    override fun deepCopy(): Layer {
        return CompositionLayer(composition.deepCopy())
    }
}

internal const val CONTAINER_NAME = "__compottie_container"
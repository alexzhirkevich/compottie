package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionComposition
import io.github.alexzhirkevich.compottie.internal.assets.PrecompositionAsset
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("0")
@Serializable
internal data class PrecompositionLayer(
    val refId : String,

    @SerialName("w")
    override var width : Float,

    @SerialName("h")
    override var height : Float,

    @SerialName("tm")
    override val timeRemapping : AnimatedNumber? = null,

    @SerialName("ddd")
    override val is3d: BooleanInt = BooleanInt.No,

    @SerialName("ind")
    override val index: Int? = null,

    @SerialName("ip")
    override val inPoint: Float? = null,

    @SerialName("op")
    override val outPoint: Float? = null,

    @SerialName("st")
    override val startTime: Float? = null,

    @SerialName("nm")
    override val name: String? = null,

    @SerialName("sr")
    override val timeStretch: Float = 1f,

    @SerialName("parent")
    override val parent: Int? = null,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("masksProperties")
    override val masks: List<Mask>? = null,

    @SerialName("hasMask")
    override val hasMask: Boolean? = null,

    @SerialName("ef")
    override var effects: List<LayerEffect> = emptyList(),

    @SerialName("ks")
    override val transform: Transform = Transform(),

    @SerialName("ao")
    override val autoOrient: BooleanInt = BooleanInt.No,

    @SerialName("tt")
    override val matteMode: MatteMode? = null,

    @SerialName("tp")
    override val matteParent: Int? = null,

    @SerialName("td")
    override val matteTarget: BooleanInt? = null,

    @SerialName("bm")
    override val blendMode: LottieBlendMode = LottieBlendMode.Normal,

    @SerialName("cl")
    override val clazz: String? = null,

    @SerialName("ln")
    override val htmlId: String? = null,

    @SerialName("ct")
    override val collapseTransform: BooleanInt = BooleanInt.No
) : BaseCompositionLayer() {

    internal val composition = object : ExpressionComposition {
        override val name: String?
            get() = this@PrecompositionLayer.name
        override val width: Float
            get() = this@PrecompositionLayer.width
        override val height: Float
            get() = this@PrecompositionLayer.height
        override val startTime: Float
            get() {
                val ip = inPoint ?: return 0f
                val op = outPoint ?: return 0f
                val dur = (op - ip).takeIf { it != 0f } ?: return 0f
                return (this@PrecompositionLayer.startTime ?: 0f) / dur
            }

        override val layersByName: Map<String, Layer> by lazy {
            this@PrecompositionLayer.loadedLayers
                .orEmpty()
                .associateBy { it.name.orEmpty() }
        }

        override val layersByIndex: Map<Int, Layer> by lazy {
            this@PrecompositionLayer.loadedLayers
                .orEmpty()
                .associateBy { it.index ?: Int.MIN_VALUE }
        }

        override val layersCount: Int
            get() = loadedLayers?.size ?: 0

        override fun transformMatrix(state: AnimationState): Matrix {
            return totalTransformMatrix(state)
        }
    }

    override fun compose(state: AnimationState): List<Layer> {
        return (state.assets[refId] as? PrecompositionAsset?)?.layers.orEmpty().map {
            it.deepCopy()
        }
    }

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {
        state.onComposition(composition) {
            super.drawLayer(drawScope, parentMatrix, parentAlpha, it)
        }
    }

    override fun deepCopy(): Layer {
        return PrecompositionLayer(
            refId = refId,
            width = width,
            height = height,
            timeRemapping = timeRemapping,
            is3d = is3d,
            index = index,
            inPoint = inPoint,
            outPoint = outPoint,
            startTime = startTime,
            name = name,
            timeStretch = timeStretch,
            parent = parent,
            hidden = hidden,
            masks = masks?.map(Mask::deepCopy),
            hasMask = hasMask,
            effects = effects.map(LayerEffect::copy),
            transform = transform.deepCopy(),
            autoOrient = autoOrient,
            matteMode = matteMode,
            matteParent = matteParent,
            matteTarget = matteTarget,
            blendMode = blendMode,
            clazz = clazz,
            htmlId = htmlId,
            collapseTransform = collapseTransform
        )
    }
}
package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.dynamic.DynamicLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.ContentGroup
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.shapes.Shape
import io.github.alexzhirkevich.compottie.internal.shapes.TransformShape
import io.github.alexzhirkevich.compottie.internal.utils.firstInstanceOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("4")
internal class ShapeLayer(

    @SerialName("ks")
    override val transform: Transform = Transform(),

    @SerialName("ao")
    override val autoOrient: BooleanInt = BooleanInt.No,

    @SerialName("ddd")
    override val is3d: BooleanInt = BooleanInt.No,

    @SerialName("ind")
    override val index: Int? = null,

    @SerialName("bm")
    override val blendMode: LottieBlendMode = LottieBlendMode.Normal,

    @SerialName("cl")
    override val clazz: String? = null,

    @SerialName("ln")
    override val htmlId: String? = null,

    @SerialName("ip")
    override val inPoint: Float? = null,

    @SerialName("op")
    override val outPoint: Float? = null,

    @SerialName("nm")
    override val name: String? = null,

    @SerialName("ef")
    override var effects: List<LayerEffect> = emptyList(),

    @SerialName("sr")
    override val timeStretch: Float = 1f,

    @SerialName("parent")
    override val parent: Int? = null,

    @SerialName("tt")
    override val matteMode: MatteMode? = null,

    @SerialName("tp")
    override val matteParent: Int? = null,

    @SerialName("td")
    override val matteTarget: BooleanInt? = null,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("ct")
    override val collapseTransform: BooleanInt = BooleanInt.No,

    @SerialName("masksProperties")
    override val masks: List<Mask>? = null,

    @SerialName("shapes")
    val shapes: List<Shape> = emptyList(),
) : BaseLayer() {

    init {
        shapes.forEach {
            it.layer = this
        }
    }

    @Transient
    private var dynamicLayer : DynamicLayerProvider? = null
        set(value) {
            if (field != value) {
                field = value
                if (value is DynamicShapeLayerProvider) {
                    shapes.fastForEach {
                        it.setDynamicProperties(null, value)
                    }
                }
            }
        }

    @Transient
    private val contentGroup = ContentGroup(
        name = name,
        hidden = { dynamicLayer?.hidden.derive(hidden, it) },
        contents = shapes,
        transform = shapes.firstInstanceOf<TransformShape>()?.apply {
            autoOrient = this@ShapeLayer.autoOrient == BooleanInt.Yes
        }
    ).apply {
        setContents(emptyList(), emptyList())
    }

    override fun drawLayer(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, state: AnimationState) {
        contentGroup.draw(drawScope, parentMatrix, parentAlpha, state)
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect,
    ) {
        resolvingPath?.let {
            dynamicLayer = state.dynamic?.get(it)
        }

        (dynamicLayer as? DynamicShapeLayerProvider?)?.let { dp ->
            shapes.fastForEach {
                it.setDynamicProperties(null, dp)
            }
        }
        super.getBounds(drawScope, parentMatrix, applyParents, state, outBounds)
        contentGroup.getBounds(drawScope, boundsMatrix, applyParents, state, outBounds)
    }
}

package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import io.github.alexzhirkevich.compottie.dynamic.DynamicImageLayerProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.assets.ImageAsset
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsState
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.helpers.asComposeBlendMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("2")
internal class ImageLayer(
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

    @SerialName("st")
    override val startTime: Float? = null,

    @SerialName("nm")
    override val name: String? = null,

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

    override val hasMask: Boolean? = null,

    @SerialName("ef")
    override var effects: List<LayerEffect> = emptyList(),

    @SerialName("refId")
    val refId : String,
) : BaseLayer() {

    @Transient
    private val paint = Paint().apply {
        isAntiAlias = true
    }

    private val effectState by lazy {
        LayerEffectsState()
    }

    private fun dynamicAsset(state: AnimationState) : ImageAsset? {

        val dynamic = dynamicLayer as? DynamicImageLayerProvider

        val asset = state.assets[refId] as? ImageAsset ?: return null
        val image = dynamic?.image?.invoke(state, asset.spec) ?: return asset
        asset.setBitmap(image)

        return asset
    }

    override fun drawLayer(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, state: AnimationState) {
        val mAsset = dynamicAsset(state) ?: return
        val bitmap = mAsset.bitmap ?: return

        paint.alpha = parentAlpha
        paint.blendMode = blendMode.asComposeBlendMode()

        effectsApplier.applyTo(paint, state, effectState)

        drawScope.drawIntoCanvas { canvas ->
            canvas.save()
            canvas.concat(parentMatrix)

            drawScope.drawIntoCanvas {
                it.drawImage(bitmap, Offset.Zero, paint)
            }
            canvas.restore()
        }
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect
    ) {
        super.getBounds(drawScope, parentMatrix, applyParents, state, outBounds)

        dynamicAsset(state)?.let {
            outBounds.set(
                left = 0f,
                top = 0f,
                right = it.width.toFloat(),
                bottom = it.height.toFloat()
            )
            boundsMatrix.map(outBounds)
        }
    }

    override fun deepCopy(): Layer {
        return ImageLayer(
            transform = transform.deepCopy(),
            autoOrient = autoOrient,
            is3d = is3d,
            index = index,
            blendMode = blendMode,
            clazz = clazz,
            htmlId = htmlId,
            inPoint = inPoint,
            outPoint = outPoint,
            startTime = startTime,
            name = name,
            timeStretch = timeStretch,
            parent = parent,
            matteMode = matteMode,
            matteParent = matteParent,
            matteTarget = matteTarget,
            hidden = hidden,
            collapseTransform = collapseTransform,
            masks = masks?.map(Mask::deepCopy),
            hasMask = hasMask,
            effects = effects.map(LayerEffect::copy),
            refId = refId
        )
    }
}


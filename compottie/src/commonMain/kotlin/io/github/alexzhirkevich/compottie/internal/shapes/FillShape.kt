package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.dynamic.DynamicFillProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicSolidDrawProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsState
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.FillRule
import io.github.alexzhirkevich.compottie.internal.helpers.asComposeBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.asPathFillType
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("fl")
internal class FillShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    val withAlpha : BooleanInt = BooleanInt.No,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("o")
    val opacity : AnimatedNumber?,

    @SerialName("c")
    val color : AnimatedColor,

    @SerialName("r")
    val fillRule : FillRule? = null,

    ) : Shape, DrawingContent {

    @Transient
    override lateinit var layer: Layer

    @Transient
    private val path = Path().apply {
        fillRule?.asPathFillType()?.let {
            fillType = it
        }
    }

    @Transient
    private var paths: List<PathContent> = emptyList()

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    @Transient
    private var dynamic : DynamicFillProvider? = null

    @Transient
    private var roundShape : RoundShape? = null

    private val effectsState by lazy {
        LayerEffectsState()
    }

    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, state: AnimationState) {

        if (hidden) {
            return
        }

        var c = color.interpolated(state)

        (dynamic as? DynamicSolidDrawProvider)?.color?.let {
            c = it.derive(c, state)
        }

        paint.color = c

        var alpha = 1f

        opacity?.interpolatedNorm(state)?.let {
            alpha = (alpha * it).coerceIn(0f,1f)
        }
        dynamic?.opacity?.let {
            alpha = it.derive(alpha, state).coerceIn(0f,1f)
        }

        paint.alpha = (alpha * parentAlpha).coerceIn(0f,1f)
        paint.colorFilter = dynamic?.colorFilter.derive(paint.colorFilter, state)
        paint.blendMode = dynamic?.blendMode.derive(paint.blendMode, state)

        roundShape?.applyTo(paint, state)

        layer.effectsApplier.applyTo(paint, state, effectsState)
        path.reset()

        paths.fastForEach {
            path.addPath(it.getPath(state), parentMatrix)
        }

        drawScope.drawIntoCanvas { canvas ->
            canvas.drawPath(path, paint)
        }
    }
    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect
    ) {

        path.reset()
        paths.fastForEach {
            path.addPath(it.getPath(state), parentMatrix)
        }

        outBounds.set(path.getBounds())
        // Add padding to account for rounding errors.
        outBounds.set(
            outBounds.left - 1,
            outBounds.top - 1,
            outBounds.right + 1,
            outBounds.bottom + 1
        )
    }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider) {
        super.setDynamicProperties(basePath, properties)
        if (name != null) {
            dynamic = properties[layerPath(basePath, name)]
        }
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        paths = contentsAfter.filterIsInstance<PathContent>()

        roundShape = contentsBefore.find { it is RoundShape } as? RoundShape
    }
}
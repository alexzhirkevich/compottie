package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.dynamic.DynamicFillProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeProvider
import io.github.alexzhirkevich.compottie.dynamic.applyToPaint
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.GradientColors
import io.github.alexzhirkevich.compottie.internal.animation.GradientType
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffectsState
import io.github.alexzhirkevich.compottie.internal.helpers.FillRule
import io.github.alexzhirkevich.compottie.internal.helpers.asComposeBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.asPathFillType
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.platform.GradientShader
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.utils.firstInstanceOf
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("gf")
internal class GradientFillShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("o")
    val opacity : AnimatedNumber? = null,

    @SerialName("s")
    val startPoint : AnimatedVector2,

    @SerialName("e")
    val endPoint : AnimatedVector2,

    @SerialName("t")
    val type : GradientType,

    /**
     * Gradient Highlight Length. Only if type is Radial
     * */
    @SerialName("h")
    val highlightLength : AnimatedNumber? = null,

    /**
     * Highlight Angle. Only if type is Radial
     * */
    @SerialName("a")
    val highlightAngle : AnimatedNumber? = null,

    @SerialName("g")
    val colors : GradientColors,

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
    private val boundsRect = MutableRect(0f,0f,0f,0f)

    @Transient
    private var paths: List<PathContent> = emptyList()

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            blendMode = layer.blendMode.asComposeBlendMode()
        }
    }

    @Transient
    private var dynamicFill : DynamicFillProvider? = null
    @Transient
    private var dynamicShape : DynamicShapeProvider? = null


    @Transient
    private val gradientCache = LinkedHashMap<Int, Shader>()

    @Transient
    private var roundShape : RoundShape? = null

    private val effectsState by lazy {
        LayerEffectsState()
    }
    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, state: AnimationState) {

        if (dynamicShape?.hidden.derive(hidden, state)) {
            return
        }

        if (dynamicFill?.gradient == null) {
            paint.shader = GradientShader(
                type = type,
                startPoint = startPoint,
                endPoint = endPoint,
                colors = colors,
                state = state,
                matrix = parentMatrix,
                cache = gradientCache
            )
        } else {
            getBounds(drawScope, parentMatrix, false, state, boundsRect)
        }

        dynamicFill.applyToPaint(
            paint = paint,
            state = state,
            parentAlpha = parentAlpha,
            opacity = opacity,
            parentMatrix = parentMatrix,
            size = boundsRect.size,
            gradientCache = gradientCache
        )

        layer.effectsApplier.applyTo(paint, state, effectsState)

        path.reset()

        paths.fastForEach {
            path.addPath(it.getPath(state), parentMatrix)
        }

        roundShape?.applyTo(paint, state)

        drawScope.drawIntoCanvas {
            it.drawPath(path, paint)
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
            dynamicFill = properties[layerPath(basePath, name)]
            dynamicShape = properties[layerPath(basePath, name)]
        }
    }


    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        paths = contentsAfter.filterIsInstance<PathContent>()
        roundShape = contentsBefore.firstInstanceOf()
    }
}


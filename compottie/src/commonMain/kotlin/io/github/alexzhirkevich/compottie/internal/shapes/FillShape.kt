package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
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

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            layer.blendMode.asComposeBlendMode()
        }
    }

    @Transient
    private var roundShape : RoundShape? = null

    private var lastBlurRadius : Float? = null

    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, state: AnimationState) {

        if (hidden) {
            return
        }

        paint.color = color.interpolated(state)

        paint.alpha = opacity?.interpolated(state)?.let {
            (parentAlpha * it / 100f).coerceIn(0f, 1f)
        } ?: parentAlpha

        roundShape?.applyTo(paint, state)

        lastBlurRadius = layer.applyBlurEffectIfNeeded(paint, state, lastBlurRadius)

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

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        paths = contentsAfter.filterIsInstance<PathContent>()

        roundShape = contentsBefore?.find { it is RoundShape } as? RoundShape
    }
}
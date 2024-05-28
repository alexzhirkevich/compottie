package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.schema.Content
import io.github.alexzhirkevich.compottie.internal.schema.DrawableContent
import io.github.alexzhirkevich.compottie.internal.schema.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
sealed interface Shape : Content {
    val matchName : String?


    val hidden : Boolean
}


@Serializable
internal sealed class SolidDrawShape : Shape, DrawableContent {

    abstract val withAlpha : BooleanInt
    abstract val opacity : Value
    abstract val color : Vector
    abstract val paintingStyle: PaintingStyle

    private val cachedColor by lazy {
        if (opacity is Value.Default && color is Vector.Default) {
            interpolatedColor(0)
        } else null
    }

    private val paths = mutableListOf<PathContent>()

    private val paint = Paint()

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentsAfter.forEach {
            if (it is PathContent){
                paths += it
            }
        }
    }

    open fun setupPaint(paint: Paint, time: Int) {
        paint.isAntiAlias = true
        paint.style = paintingStyle
        paint.alpha = interpolatedAlpha(time)
        paint.color = cachedColor ?: interpolatedColor(time)
    }

    override fun drawIntoCanvas(canvas: Canvas, parentMatrix: Matrix, time: Int) {
        if (hidden) {
            return
        }
        setupPaint(paint, time)

        paths.fastForEach {
            canvas.drawPath(it.getPath(time), paint)
        }
    }

    private fun interpolatedAlpha(time : Int)=
        (opacity.interpolated(time) / 100f).coerceIn(0f,1f)

    private fun interpolatedColor(time: Int): Color {
        return color.interpolated(0).let {
            Color(
                red = it[0],
                green = it[1],
                blue = it[2],
                alpha = if (withAlpha.toBoolean()) {
                    it[3] * interpolatedAlpha(time)
                } else interpolatedAlpha(time)
            )
        }
    }
}
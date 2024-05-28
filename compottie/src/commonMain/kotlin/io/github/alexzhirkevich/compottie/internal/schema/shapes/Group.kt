package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("gr")
internal class Group(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("np")
    val numberOfProperties : Int = 0,

    @SerialName("it")
    val items : List<Shape> = emptyList(),
) : CanvasShape by BaseCanvasShape(
    root = false,
    hidden = hidden,
    shapes = items,
    matchName = matchName,
    name = name
)

internal class BaseCanvasShape(
    val root : Boolean,
    override val hidden: Boolean,
    override val shapes : List<Shape>,
    override val matchName: String? = null,
    override val name: String? = null,
) : CanvasShape {

    private val visibleShapes = shapes.filterNot(Shape::hidden)

    private val drawShapes = visibleShapes
        .filterIsInstance<DrawShape>().map {
            it to Paint()
        }

    override var path = Path()

    private val layoutShapes = visibleShapes
        .filterIsInstance<LayoutShape>()

    private val groupShapes = visibleShapes
        .filterIsInstance<CanvasShape>()
        .onEach {
            it.path = path
        }

    override fun drawIntoCanvas(canvas: Canvas, time: Int) {

        if (drawShapes.isEmpty()) {
            groupShapes.fastForEach {
                it.drawIntoCanvas(canvas, time)
            }
        } else {
            drawShapes.fastForEach { (drawShape, paint) ->
                if (root) {
                    path.rewind()
                }
                drawShape.applyTo(paint, time)
                groupShapes.fastForEach {
                    it.drawIntoCanvas(canvas, time)
                }
                layoutShapes.fastForEach { layoutShape ->
                    layoutShape.applyTo(path, time)
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
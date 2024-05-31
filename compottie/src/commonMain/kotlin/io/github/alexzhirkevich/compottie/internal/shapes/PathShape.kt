package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.content.ShapeModifierContent
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.CompoundTrimPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("sh")
internal class PathShape(
    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("ks")
    val shape : AnimatedShape
) : Shape, PathContent {

    @Transient
    private val trimPaths: CompoundTrimPath = CompoundTrimPath()

    private val path = Path()
    override fun getPath(frame: Float): Path {
        path.reset()

        if (hidden) {
            return path
        }
        path.set(shape.interpolated(frame))
        path.fillType = PathFillType.EvenOdd

        trimPaths.apply(path, frame)
        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        val shapeModifierContents: MutableList<ShapeModifierContent> = mutableListOf()

        contentsBefore.fastForEach { content ->
            if (content.isSimultaneousTrimPath()) {
                // Trim path individually will be handled by the stroke where paths are combined.
                trimPaths.addTrimPath(content)
            } else if (content is ShapeModifierContent) {
                shapeModifierContents.add(content)
            }
        }

        shape.shapeModifiers = shapeModifierContents
    }
}
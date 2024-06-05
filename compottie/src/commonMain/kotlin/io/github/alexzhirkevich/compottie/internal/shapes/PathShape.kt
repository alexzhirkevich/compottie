package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.platform.set
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.helpers.CompoundTrimPath
import io.github.alexzhirkevich.compottie.internal.layers.Layer
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
    override lateinit var layer: Layer

    @Transient
    private val trimPaths: CompoundTrimPath = CompoundTrimPath()

    private val path = Path()

    override fun getPath(state: AnimationState): Path {
        path.reset()

        if (hidden) {
            return path
        }
        path.set(shape.interpolated(state))
        path.fillType = PathFillType.EvenOdd

        trimPaths.apply(path, state)
        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

        contentsBefore.fastForEach { content ->
            if (content.isSimultaneousTrimPath()) {
                // Trim path individually will be handled by the stroke where paths are combined.
                trimPaths.addTrimPath(content)
            }
        }
    }
}
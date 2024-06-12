package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.ContentGroupBase
import io.github.alexzhirkevich.compottie.internal.content.GreedyContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.platform.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmInline


@Serializable
@JvmInline
internal value class MergeMode(val mode : Byte) {
    companion object {
        val Normal = MergeMode(1)
        val Add = MergeMode(2)
        val Subtract = MergeMode(3)
        val Intersect = MergeMode(4)
        val ExcludeIntersections = MergeMode(5)
    }
}

@Serializable
@SerialName("mm")
internal class MergePathsShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("mm")
    val mode : MergeMode = MergeMode.Normal
) : Shape, GreedyContent, PathContent {

    @Transient
    override lateinit var layer: Layer

    @Transient
    private val path = Path()

    @Transient
    private val remainderPath = Path()

    @Transient
    private val firstPath = Path()

    @Transient
    private var pathContents = emptyList<PathContent>()

    override fun getPath(state: AnimationState): Path {

        path.reset()

        if (hidden) {
            return path
        }

        when (mode) {
            MergeMode.Normal -> pathContents.fastForEach {
                path.addPath(it.getPath(state))
            }
            MergeMode.Add -> opFirstPathWithRest(PathOperation.Union, state)
            MergeMode.Subtract -> opFirstPathWithRest(PathOperation.Difference, state)
            MergeMode.Intersect -> opFirstPathWithRest(PathOperation.Intersect, state)
            MergeMode.ExcludeIntersections -> opFirstPathWithRest(PathOperation.Xor, state)
        }

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        pathContents.fastForEach {
            it.setContents(contentsBefore, contentsAfter)
        }
    }

    override fun absorbContent(contents: MutableList<Content>) {

        val thisIndex = contents.indexOf(this).takeIf { it > 0 } ?: return

        pathContents = contents
            .take(thisIndex)
            .filterIsInstance<PathContent>()
            .reversed()

        if (pathContents.size < 2) {
            contents.removeAt(thisIndex)
        } else {
            contents.removeAll(pathContents)
        }
    }

    private fun opFirstPathWithRest(op: PathOperation, state: AnimationState) {

        remainderPath.reset()
        firstPath.reset()

        for (i in pathContents.size - 1 downTo 1) {
            val content = pathContents[i]

            if (content is ContentGroupBase) {
                content.pathContents.fastForEachReversed { path ->
                    val p = path.getPath(state)
                    content.transform?.matrix(state)?.let(p::transform)
                    remainderPath.addPath(p)
                }
            } else {
                remainderPath.addPath(content.getPath(state))
            }
        }

        val lastContent = pathContents[0]
        if (lastContent is ContentGroupBase) {
            lastContent.pathContents.fastForEach {
                val p = it.getPath(state)
//                lastContent.transform?.matrix(state)?.let(p::transform)
                firstPath.addPath(p)
            }
        } else {
            firstPath.set(lastContent.getPath(state))
        }


//        firstPath.reset()
//        firstPath.addRect(bounds)
        path.op(firstPath, remainderPath, op)
    }
}

package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
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
    private val pathContents = mutableListOf<PathContent>()

    override fun getPath(frame: Float): Path {

        path.reset()

        if (hidden) {
            return path
        }

        when (mode) {
            MergeMode.Normal -> addPaths(frame)
            MergeMode.Add -> opFirstPathWithRest(PathOperation.Union, frame)
            MergeMode.Subtract -> opFirstPathWithRest(PathOperation.Difference, frame)
            MergeMode.Intersect -> opFirstPathWithRest(PathOperation.Intersect, frame)
            MergeMode.ExcludeIntersections -> opFirstPathWithRest(PathOperation.Xor, frame)
        }

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        pathContents.fastForEach {
            it.setContents(contentsBefore, contentsAfter)
        }
    }

    override fun absorbContent(contents: MutableListIterator<Content>) {

        // Fast forward the iterator until after this content.
        @Suppress("ControlFlowWithEmptyBody")
        while (contents.hasPrevious() && contents.previous() !== this) {
        }
        while (contents.hasPrevious()) {
            val content = contents.previous()
            if (content is PathContent) {
                pathContents.add(content)
                contents.remove()
            }
        }
    }

    private fun addPaths(frame: Float) {
        pathContents.fastForEach {
            path.addPath(it.getPath(frame))
        }
    }

    private fun opFirstPathWithRest(op: PathOperation, frame: Float) {

        remainderPath.reset()
        firstPath.reset()

        for (i in pathContents.size - 1 downTo 1) {
            val content = pathContents[i]

            if (content is ContentGroupBase) {
                content.pathContents.fastForEachReversed { path ->
                    val p = path.getPath(frame)
                    content.transform?.matrix(frame)?.let {
                        p.transform(it)
                    }
                    remainderPath.addPath(p)
                }
            } else {
                remainderPath.addPath(content.getPath(frame))
            }
        }

        val lastContent = pathContents[0]
        if (lastContent is ContentGroupBase) {
            lastContent.pathContents.fastForEach {
                val path = it.getPath(frame)
                lastContent.transform?.matrix(frame)?.let(path::transform)
                firstPath.addPath(path)
            }
        } else {
            firstPath.set(lastContent.getPath(frame))
        }

        if (pathContents.size == 1) {
            path.addPath(firstPath)
            return
        }

        path.op(firstPath, remainderPath, op)
    }
}

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
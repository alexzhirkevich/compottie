package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.RepeaterTransform
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.ContentGroup
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.GreedyContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.utils.preConcat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("rp")
internal class RepeaterShape(

    @SerialName("c")
    val copies : AnimatedNumber,

    @SerialName("o")
    val offset : AnimatedNumber? = null,

    @SerialName("tr")
    val transform: RepeaterTransform,

    @SerialName("nm")
    override val name: String? = null,

    @SerialName("mn")
    override val matchName: String? = null,

    @SerialName("hd")
    override val hidden: Boolean = false
) : Shape, GreedyContent, DrawingContent, PathContent {

    @Transient
    override lateinit var layer: Layer

    @Transient
    private var contentGroup: ContentGroup? = null

    @Transient
    private val path = Path()

    @Transient
    private val matrix = Matrix()

    override fun draw(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {
        contentGroup?.let { contentGroup ->
            val copies = copies.interpolated(state)
            val offset = offset?.interpolated(state) ?: 0f
            val startOpacity = transform.startOpacity?.interpolated(state)?.div(100f) ?: 1f
            val endOpacity = transform.endOpacity?.interpolated(state)?.div(100f) ?: 1f

            for (i in copies.toInt() - 1 downTo 0) {
                matrix.setFrom(parentMatrix)
                matrix.preConcat(transform.repeaterMatrix(state, i + offset))
                val newAlpha = parentAlpha * lerp(startOpacity, endOpacity, i / copies)
                contentGroup.draw(drawScope, matrix, newAlpha, state)
            }
        }
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect
    ) {
        contentGroup?.getBounds(drawScope, parentMatrix, applyParents, state, outBounds)
    }

    override fun getPath(state: AnimationState): Path {
        path.reset()
        val contentPath = contentGroup?.getPath(state) ?: return path

        val copies = copies.interpolated(state)
        val offset = offset?.interpolated(state) ?: 0f

        for (i in copies.toInt() - 1 downTo 0) {
            matrix.setFrom(transform.repeaterMatrix(state, i + offset))
            path.addPath(contentPath, matrix)
        }
        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentGroup?.setContents(contentsBefore, contentsAfter)

    }

    override fun absorbContent(contents: MutableListIterator<Content>) {

        // This check prevents a repeater from getting added twice.
        // This can happen in the following situation:
        //    RECTANGLE
        //    REPEATER 1
        //    FILL
        //    REPEATER 2
        // In this case, the expected structure would be:
        //     REPEATER 2
        //        REPEATER 1
        //            RECTANGLE
        //        FILL
        // Without this check, REPEATER 1 will try and absorb contents once it is already inside of
        // REPEATER 2.
        if (contentGroup != null) {
            return
        }

        // Fast forward the iterator until after this content.
        @Suppress("ControlFlowWithEmptyBody")
        while (contents.hasPrevious() && contents.previous() !== this) {
        }

        val contentsList = mutableListOf<Content>()

        while (contents.hasPrevious()) {
            contentsList.add(contents.previous())
            contents.remove()
        }
        contentsList.reverse()

        contentGroup = ContentGroup(name, hidden, contentsList, null)
    }
}
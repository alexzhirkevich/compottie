package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("rd")
internal class RoundShape(
    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    override val hidden : Boolean = false,

    @SerialName("r")
    val radius : AnimatedValue,
) : Shape {

    @Transient
    override lateinit var layer: Layer

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }
}

internal fun RoundShape.applyTo(paint: Paint, state: AnimationState){
    if (!hidden){
        val radius = radius.interpolated(state)
        val effect = PathEffect.cornerPathEffect(radius)
        paint.pathEffect = if (paint.pathEffect == null) {
            effect
        } else {
            PathEffect.chainPathEffect(effect, paint.pathEffect!!)
        }
    }
}

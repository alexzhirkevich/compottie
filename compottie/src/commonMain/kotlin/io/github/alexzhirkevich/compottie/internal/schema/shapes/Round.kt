package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("rd")
internal class Round(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("r")
    val radius : AnimatedValue,
) : DrawShape {

    override fun applyTo(paint: Paint, time: Int) {

        val radius = radius.interpolated(time)

        paint.pathEffect = PathEffect.cornerPathEffect(radius)
    }

}
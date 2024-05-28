package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class GradientColors(

    @SerialName("k")
    val colors : Vector,

    @SerialName("p")
    val numberOfColors : Int = 0
) {
    fun interpolated(time : Int) : List<Pair<Float, Color>> {

        val vector = colors.interpolated(time)
    }
}
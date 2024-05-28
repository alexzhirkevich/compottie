package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.PaintingStyle
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("fl")
internal class Fill(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    override val withAlpha : BooleanInt = BooleanInt.No,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("o")
    override val opacity : AnimatedValue,

    @SerialName("c")
    override val color : AnimatedVector2,
) : SolidDrawShape() {

    override val paintingStyle: PaintingStyle
        get() = PaintingStyle.Fill
}
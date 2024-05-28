package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.PaintingStyle
import io.github.alexzhirkevich.compottie.internal.schema.Content
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
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
    override val opacity : Value,

    @SerialName("c")
    override val color : Vector,
) : SolidDrawShape() {

    override val paintingStyle: PaintingStyle
        get() = PaintingStyle.Fill
}
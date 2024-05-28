package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.schema.helpers.LottieTransform
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("tr")
internal class TransformShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    override val anchorPoint : AnimatedVector2 = AnimatedVector2.Default(value = floatArrayOf(0f, 0f, 0f)),

    @SerialName("p")
    override val position : AnimatedVector2? = null,

    @SerialName("s")
    override val scale : AnimatedVector2? = null,

    @SerialName("r")
    override val rotation : AnimatedValue ? = null,

    @SerialName("o")
    override val opacity : AnimatedValue = AnimatedValue.Default(value = 100f),

    @SerialName("sk")
    override val skew: AnimatedValue? = null,

    @SerialName("sa")
    override val skewAxis: AnimatedValue? = null,
) : LottieTransform(), ModifierShape {

    override fun applyTo(path: Path, paint: Paint, time: Int) {
        path.transform(matrix(time))
        paint.alpha *= opacity.interpolated(time) / 100f
    }
}
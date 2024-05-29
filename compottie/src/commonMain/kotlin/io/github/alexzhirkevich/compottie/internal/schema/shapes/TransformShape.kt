package io.github.alexzhirkevich.compottie.internal.schema.shapes

import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.schema.ModifierContent
import io.github.alexzhirkevich.compottie.internal.schema.helpers.AnimatedTransform
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
) : AnimatedTransform(), Shape, ModifierContent {
    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }
}
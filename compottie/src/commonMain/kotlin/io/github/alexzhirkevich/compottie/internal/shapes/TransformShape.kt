package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.ModifierContent
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    override val anchorPoint : AnimatedVector2? = null,

    @SerialName("p")
    override val position : AnimatedVector2? = null,

    @SerialName("s")
    override val scale : AnimatedVector2? = null,

    @SerialName("r")
    override val rotation : AnimatedNumber ? = null,

    @SerialName("o")
    override val opacity : AnimatedNumber? = null,

    @SerialName("sk")
    override val skew: AnimatedNumber? = null,

    @SerialName("sa")
    override val skewAxis: AnimatedNumber? = null,
) : AnimatedTransform(), Shape, ModifierContent {

    @Transient
    override lateinit var layer: Layer

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }
}


package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.internal.content.ContentGroup
import io.github.alexzhirkevich.compottie.internal.content.ContentGroupBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("gr")
internal class GroupShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("np")
    val numberOfProperties : Int = 0,

    @SerialName("it")
    val items : List<Shape> = emptyList(),

) : Shape, ContentGroupBase by ContentGroup(
    name = name,
    hidden = hidden,
    contents = items,
    transform = items.findTransform()
)
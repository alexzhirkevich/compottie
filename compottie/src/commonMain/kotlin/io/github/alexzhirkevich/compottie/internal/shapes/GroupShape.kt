package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.internal.content.ContentGroup
import io.github.alexzhirkevich.compottie.internal.content.ContentGroupBase
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.NullLayer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
){

    @Transient
    override var layer: Layer = NullLayer()
        set(value) {
            field = value
            items.forEach {
                it.layer = value
            }

            transform?.autoOrient = value.autoOrient == BooleanInt.Yes
        }
}
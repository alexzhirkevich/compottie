package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.LayerPathSeparator
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.content.ContentGroup
import io.github.alexzhirkevich.compottie.internal.content.ContentGroupBase
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import io.github.alexzhirkevich.compottie.internal.layers.NullLayer
import io.github.alexzhirkevich.compottie.internal.utils.firstInstanceOf
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
    transform = items.firstInstanceOf()
) {

    @Transient
    override var layer: Layer = NullLayer()
        set(value) {
            field = value
            items.forEach {
                it.layer = value
            }

            transform?.autoOrient = value.autoOrient == BooleanInt.Yes
        }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider) {
        super.setDynamicProperties(basePath, properties)
        if (name != null) {
            items.forEach {
                it.setDynamicProperties(layerPath(basePath, name), properties)
            }
        }
    }
}


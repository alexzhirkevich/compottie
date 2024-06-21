package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.ContentGroupImpl
import io.github.alexzhirkevich.compottie.internal.content.ContentGroup
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

    @SerialName("it")
    val items : List<Shape> = emptyList(),

) : Shape, ContentGroup by ContentGroupImpl(
    name = name,
    hidden = { hidden },
    contents = items,
    transform = items.firstInstanceOf()
) {

    @Transient
    private var dynamicShape : DynamicShapeProvider? = null

    override fun hidden(state: AnimationState): Boolean {
        return dynamicShape?.hidden.derive(hidden, state)
    }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider) {
        super.setDynamicProperties(basePath, properties)
        if (name != null) {
            val path = layerPath(basePath, name)
            dynamicShape = properties[path]
            items.forEach {
                it.setDynamicProperties(path, properties)
            }
        }
    }
}


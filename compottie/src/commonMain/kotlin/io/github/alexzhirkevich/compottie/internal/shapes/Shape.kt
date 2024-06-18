package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface Shape : Content {
    val matchName : String?

    val hidden : Boolean

    var layer : Layer

    fun setDynamicProperties(basePath: String?, properties : DynamicShapeLayerProvider) {}

    @Serializable
    data object UnsupportedShape : Shape {
        override val name: String? get() = null

        override val matchName: String? get() = null
        override val hidden: Boolean get() = true
        override var layer: Layer
            get() = error("Unsupported shape doesn't have layer")
            set(value) {}

        override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {}
    }
}


package io.github.alexzhirkevich.compottie.internal.shapes

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

    @Serializable
    data object UnsupportedShape : Shape {
        override val name: String? get() = null

        override val matchName: String? get() = null
        override val hidden: Boolean get() = true
        override var layer: Layer
            get() = TODO("Not yet implemented")
            set(value) {}

        override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        }
    }
}


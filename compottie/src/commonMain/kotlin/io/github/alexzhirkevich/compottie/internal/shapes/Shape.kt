package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.content.Content
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed interface Shape : Content, ExpressionHolder {

    val matchName: String?

    val hidden: Boolean

    fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider?) {}

    @Serializable
    class Unsupported : Shape {
        override val name: String? get() = null

        override val matchName: String? get() = null
        override val hidden: Boolean get() = true
        override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {}
        override fun prepareExpressions(state: AnimationState) {

        }

        override fun deepCopy(): Shape = Unsupported()
    }

    fun deepCopy(): Shape
}

internal const val DIRECTION_REVERSED : Int = 3

package io.github.alexzhirkevich.compottie.internal.animation

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.text.TextDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class AnimatedTextDocument(

    @SerialName("k")
    val keyframes : List<TextDocumentKeyframe>,

    @SerialName("x")
    val expression : String? = null,

    @SerialName("sid")
    val slotID : String? = null
) : KeyframeAnimation<TextDocument> {

    private val document = TextDocument()

    @Transient
    private val delegate = BaseKeyframeAnimation(
        expression = expression,
        keyframes = keyframes,
        emptyValue = document,
        map = { s, e, p ->
            //TODO: lerp properties?
            if (p != 1.0f) s else e
        }
    )

    override fun interpolated(state: AnimationState): TextDocument {
        return delegate.interpolated(state)
    }
}
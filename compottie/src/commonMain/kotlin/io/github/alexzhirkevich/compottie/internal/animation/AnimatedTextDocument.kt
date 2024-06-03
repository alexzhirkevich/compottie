package io.github.alexzhirkevich.compottie.internal.animation

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
        keyframes = keyframes,
        emptyValue = document,
        map = { s, e, p, _ ->

            //TODO: lerp properties?
            if (p != 1.0f) s else e
        }
    )

    override fun interpolated(frame: Float): TextDocument {
        return delegate.interpolated(frame)
    }
}
package io.github.alexzhirkevich.compottie.internal.helpers.text

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTextDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class TextData(
    @SerialName("a")
    val ranges : List<TextRange>,

    @SerialName("d")
    val document : AnimatedTextDocument,

    @SerialName("m")
    val alignment: TextAlignment,

    @SerialName("p")
    val followPath : TextFollowPath,
)




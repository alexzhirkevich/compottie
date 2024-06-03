package io.github.alexzhirkevich.compottie.internal.helpers.text

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class TextFollowPath(

    @SerialName("m")
    val mask: Mask? = null,

    @SerialName("f")
    val firstMargin : AnimatedValue? = null,

    @SerialName("l")
    val lastMargin : AnimatedValue? = null,

    @SerialName("r")
    val reversePath : AnimatedValue? = null,

    @SerialName("a")
    val forceAlignment : AnimatedValue? = null,

    @SerialName("p")
    val perpendicularToPath : AnimatedValue? = null,
)
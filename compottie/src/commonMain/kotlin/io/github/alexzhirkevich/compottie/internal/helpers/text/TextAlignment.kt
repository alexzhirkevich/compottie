package io.github.alexzhirkevich.compottie.internal.helpers.text

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import kotlinx.serialization.Serializable

@Serializable
internal class TextAlignment(
    val alignment : AnimatedVector2? = null,
    val grouping: TextGrouping? = null
)
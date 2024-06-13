package io.github.alexzhirkevich.compottie.internal.content

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt

internal interface ContentGroupBase : DrawingContent, PathContent {

    val pathContents : List<PathContent>

    val transform : AnimatedTransform?
}
package io.github.alexzhirkevich.compottie.internal.content

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform

internal interface ContentGroupBase : DrawingContent, PathContent {

    val pathContents : List<PathContent>

    val transform : AnimatedTransform?
}
package io.github.alexzhirkevich.compottie.avp.animator

import io.github.alexzhirkevich.compottie.avp.AnimatedVector

public interface AnimatorSpec {
    public suspend fun load(
        animatedVector: AnimatedVector
    ): Map<String, ObjectAnimator<*, *>>
}
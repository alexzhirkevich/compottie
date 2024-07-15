package io.github.alexzhirkevich.compottie.avp

public class AnimationTarget(
    public val name : String,
    public val animation: String
)

public class AnimatedVector(
    public val animations : List<AnimationTarget>
)


package io.github.alexzhirkevich.compottie.avp.animator

public interface AnimatorSpec {
    
    public suspend fun load() : ObjectAnimator<*,*>
}
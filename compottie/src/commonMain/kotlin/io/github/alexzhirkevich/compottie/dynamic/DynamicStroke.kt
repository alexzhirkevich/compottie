package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicStroke : DynamicDraw {
    fun width(provider: PropertyProvider<Float>) {}
}


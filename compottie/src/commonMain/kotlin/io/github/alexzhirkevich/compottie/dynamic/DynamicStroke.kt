package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicStroke : DynamicDraw {
    fun width(provider: PropertyProvider<Float>) {}

    sealed interface Solid : DynamicStroke, DynamicDraw.Solid

    sealed interface Gradient : DynamicStroke, DynamicDraw.Gradient
}


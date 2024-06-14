package io.github.alexzhirkevich.compottie.dynamic


sealed interface DynamicFill : DynamicDraw {
    sealed interface Solid : DynamicFill, DynamicDraw.Solid

    sealed interface Gradient : DynamicFill, DynamicDraw.Gradient
}


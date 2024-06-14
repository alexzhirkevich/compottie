package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicShape {

    sealed interface Ellipse : DynamicShape

    sealed interface Rest : DynamicShape
}
package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicShape {

    fun hidden(provider : PropertyProvider<Boolean>)
}
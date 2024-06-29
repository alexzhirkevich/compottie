package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicLayer {

    fun hidden(provider : PropertyProvider<Boolean>)

    fun transform(builder: DynamicTransform.() -> Unit)
}





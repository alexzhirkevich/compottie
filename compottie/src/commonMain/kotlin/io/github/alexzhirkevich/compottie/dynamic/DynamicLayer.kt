package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicLayer {

    fun transform(builder: DynamicTransform.() -> Unit)

    sealed interface Shape : DynamicLayer {

        fun fill(name: String, builder: DynamicFill.() -> Unit)

        fun stroke(name: String, builder: DynamicFill.() -> Unit)
    }

    sealed interface Text : DynamicLayer {

    }
}
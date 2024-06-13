package io.github.alexzhirkevich.compottie.dynamic

sealed interface DynamicLayer {

    fun transform(builder: DynamicTransform.() -> Unit)

    sealed interface Shape : DynamicLayer {

        fun group(name : String, shape: Shape.() -> Unit)

        fun fill(vararg path: String, builder: DynamicFill.() -> Unit)

        fun stroke(vararg path: String, builder: DynamicFill.() -> Unit)
    }
}
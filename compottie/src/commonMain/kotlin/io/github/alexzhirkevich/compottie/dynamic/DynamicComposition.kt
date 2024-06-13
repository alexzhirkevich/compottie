package io.github.alexzhirkevich.compottie.dynamic

/**
 * Lottie dynamic properties builder
 * */
sealed interface DynamicComposition {

    /**
     * Layer dynamic properties builder.
     * */
    fun shapeLayer(name: String, builder: DynamicLayer.Shape.() -> Unit)

    /**
     * Layer dynamic properties builder.
     * */
    fun layer(name: String, builder: DynamicLayer.() -> Unit)
}

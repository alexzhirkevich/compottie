package io.github.alexzhirkevich.compottie.dynamic

/**
 * Lottie dynamic properties builder
 * */
sealed interface DynamicComposition {

    /**
     * Layer dynamic properties builder.
     * */
    fun shapeLayer(vararg path: String, builder: DynamicLayer.Shape.() -> Unit)

    /**
     * Layer dynamic properties builder.
     * */
    fun layer(vararg path: String, builder: DynamicLayer.() -> Unit)
}

internal const val LayerPathSeparator = "/"

internal fun layerPath(base : String?, name : String) : String = listOfNotNull(base, name)
    .joinToString(LayerPathSeparator)



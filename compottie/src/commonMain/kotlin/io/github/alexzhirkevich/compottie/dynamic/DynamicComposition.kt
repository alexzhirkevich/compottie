package io.github.alexzhirkevich.compottie.dynamic

/**
 * Lottie dynamic properties builder
 * */
sealed interface DynamicComposition {

    /**
     * Layer dynamic properties builder.
     *
     * Path is a chain of layers names up to the required layer.
     * All layers in the chain must have a name.
     *
     * Example:
     *
     * ```
     * shapeLayer("Precomposition 1", "ShapeLayer2") {
     *     transform {
     *        //...
     *     }
     *
     *     fill("Group4", "Fill 1") {
     *         color {
     *             if (isDark) Color.White else Color.Black
     *         }
     *     }
     * }
     * ```
     * */
    fun shapeLayer(vararg path: String, builder: DynamicLayer.Shape.() -> Unit)

    /**
     * Layer dynamic properties builder.
     *
     * Path is a chain of layers names up to the required layer.
     * All layers in the chain must have a name.
     *
     * Example:
     *
     * ```
     * layer("Precomposition 1", "Layer3") {
     *     transform {
     *         scale {
     *             it * (1 - this.progress)
     *         }
     *     }
     * }
     * ```
     * */
    fun layer(vararg path: String, builder: DynamicLayer.() -> Unit)
}





package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi

@Composable
@ExperimentalCompottieApi
fun rememberLottieDynamicProperties(
    vararg keys : Any?,
    composition: DynamicProperties.() -> Unit
) : DynamicProperties {
    return remember(keys) {
        DynamicCompositionProvider().apply(composition)
    }
}

/**
 * Lottie dynamic properties builder
 * */
sealed interface DynamicProperties {

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

    /**
     * Shape layer dynamic properties builder.
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
    fun shapeLayer(vararg path: String, builder: DynamicShapeLayer.() -> Unit)

    /**
     * Image layer dynamic properties builder.
     *
     * Path is a chain of layers names up to the required layer.
     * All layers in the chain must have a name.
     * */
    fun imageLayer(vararg path: String, builder: DynamicImageLayer.() -> Unit)
}





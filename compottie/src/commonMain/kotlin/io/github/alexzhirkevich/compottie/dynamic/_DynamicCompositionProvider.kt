package io.github.alexzhirkevich.compottie.dynamic

import io.github.alexzhirkevich.compottie.internal.layers.ResolvingPath

internal class DynamicCompositionProvider : DynamicProperties {

    private val layers = mutableMapOf<String, DynamicLayerProvider>()

    val size: Int
        get() = layers.size

    override fun shapeLayer(vararg path: String, builder: DynamicShapeLayer.() -> Unit) {
        val p = path.joinToString(LayerPathSeparator)

        val provider = when(val existent = layers[p]) {
            is DynamicShapeLayerProvider -> existent
            is DynamicLayerProvider -> DynamicShapeLayerProvider().apply {
                transform = existent.transform
            }

            else -> DynamicShapeLayerProvider()
        }

        provider.apply(builder)

        layers[p] = provider
    }

    override fun imageLayer(vararg path: String, builder: DynamicImageLayer.() -> Unit) {
        layers[path.joinToString(LayerPathSeparator)] = DynamicImageLayerProvider().apply(builder)
    }

    override fun layer(vararg path: String, builder: DynamicLayer.() -> Unit) {
        layers[path.joinToString(LayerPathSeparator)] = DynamicLayerProvider().apply(builder)
    }

    operator fun get(path: ResolvingPath): DynamicLayerProvider? = layers[path.path]
}

@PublishedApi
internal const val LayerPathSeparator = "/"

internal fun layerPath(base : String?, name : String) : String = listOfNotNull(base, name)
    .joinToString(LayerPathSeparator)
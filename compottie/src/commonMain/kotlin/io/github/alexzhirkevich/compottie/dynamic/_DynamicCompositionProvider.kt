package io.github.alexzhirkevich.compottie.dynamic

internal class DynamicCompositionProvider : DynamicComposition {

    private val layers = mutableMapOf<String, DynamicLayerProvider>()

    val size: Int
        get() = layers.size

    override fun shapeLayer(vararg path: String, builder: DynamicLayer.Shape.() -> Unit) {
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

    override fun layer(vararg path: String, builder: DynamicLayer.() -> Unit) {
        layers[path.joinToString(LayerPathSeparator)] = DynamicLayerProvider().apply(builder)
    }

    operator fun get(name: String): DynamicLayerProvider? = layers[name]
}

@PublishedApi
internal const val LayerPathSeparator = "/"

internal fun layerPath(base : String?, name : String) : String = listOfNotNull(base, name)
    .joinToString(LayerPathSeparator)
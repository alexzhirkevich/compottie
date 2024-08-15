package io.github.alexzhirkevich.compottie.dynamic

import io.github.alexzhirkevich.compottie.internal.layers.ResolvingPath

private data class LayerWithPathPattern(
    val pathPattern: List<String>,
    val layer: DynamicLayerProvider
)

@PublishedApi
internal class DynamicCompositionProvider : LottieDynamicProperties {

    private val layers = mutableMapOf<String, DynamicLayerProvider>()
    private val layersByPattern = mutableListOf<LayerWithPathPattern>()

    override fun shapeLayer(vararg path: String, builder: DynamicShapeLayer.() -> Unit) {
//        val p = path.joinToString(LayerPathSeparator, LayerPathSeparator)
//
//        val provider = when(val existent = layers[p]) {
//            is DynamicShapeLayerProvider -> existent
//            is DynamicLayerProvider -> DynamicShapeLayerProvider().apply {
//                transform = existent.transform
//            }
//
//            else -> DynamicShapeLayerProvider()
//        }
//
//        provider.apply(builder)
//
//        layers[p] = provider
        appendLayer(path, DynamicShapeLayerProvider().apply(builder))
    }

    override fun imageLayer(vararg path: String, builder: DynamicImageLayer.() -> Unit) {
        appendLayer(path, DynamicImageLayerProvider().apply(builder))
    }

    override fun textLayer(vararg path: String, builder: DynamicTextLayer.() -> Unit) {
        appendLayer(path, DynamicTextLayerProvider().apply(builder))
    }

    override fun layer(vararg path: String, builder: DynamicLayer.() -> Unit) {
        appendLayer(path, DynamicLayerProvider().apply(builder))
    }

    private fun <T : DynamicLayerProvider> appendLayer(path: Array<out String>, instance: T) {
        if (path.any { it == "**" || it == "*" }) {
            layersByPattern.add(LayerWithPathPattern(pathPattern = path.toList(), layer = instance))
        } else {
            layers[path.joinToString(LayerPathSeparator, LayerPathSeparator)] = instance
        }
    }

    operator fun get(path: ResolvingPath): DynamicLayerProvider? {
        val exactLayer = layers[path.path]
        // Prioritize an exact match over a pattern match
        if (exactLayer != null) return exactLayer

        val pathParts = path.path.split(LayerPathSeparator)
        for (patternLayer in layersByPattern) {
            val (pattern, layer) = patternLayer
            if (pathMatches(path = pathParts, pattern = pattern)) return layer
        }
        return null
    }
}

@PublishedApi
internal const val LayerPathSeparator: String = "/"

internal fun layerPath(base: String?, name: String): String = listOfNotNull(base, name)
    .joinToString(LayerPathSeparator)

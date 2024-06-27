package io.github.alexzhirkevich.compottie.dynamic

import io.github.alexzhirkevich.compottie.internal.layers.ResolvingPath

@PublishedApi
internal class DynamicCompositionProvider : LottieDynamicProperties {

    private val layers = mutableMapOf<String, DynamicLayerProvider>()

    val size: Int
        get() = layers.size

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

    private fun <T : DynamicLayerProvider> appendLayer(path : Array<out String>, instance : T) {
        layers[path.joinToString(LayerPathSeparator, LayerPathSeparator)] = instance
    }

    operator fun get(path: ResolvingPath): DynamicLayerProvider? = layers[path.path]
}

@PublishedApi
internal const val LayerPathSeparator = "/"

internal fun layerPath(base : String?, name : String) : String = listOfNotNull(base, name)
    .joinToString(LayerPathSeparator)
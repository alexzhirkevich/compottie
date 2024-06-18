package io.github.alexzhirkevich.compottie.dynamic

@PublishedApi
internal class DynamicShapeLayerProvider(
    private val basePath : String? = null,
    private val root : DynamicShapeLayerProvider? = null
) : DynamicLayerProvider(), DynamicShapeLayer {

    private val nRoot get() = root ?: this

    @PublishedApi
    internal val shapes = mutableMapOf<String, DynamicShape>()

    override fun shape(vararg path: String, builder: DynamicShape.() -> Unit) {
        shapes[path.joinToString(LayerPathSeparator)] =
            DynamicShapeProvider().apply(builder)
    }

    override fun group(name: String, builder: DynamicShapeLayer.() -> Unit) {
        DynamicShapeLayerProvider(
            basePath = layerPath(basePath, name),
            root = nRoot
        ).apply(builder)
    }

    internal inline operator fun <reified S : DynamicShape> get(path: String): S? =
        shapes[path] as? S

    override fun fill(vararg path: String, builder: DynamicFill.() -> Unit) {
        shapes[path.joinToString(LayerPathSeparator)] =
            DynamicFillProvider().apply(builder)
    }

    override fun stroke(vararg path: String, builder: DynamicStroke.() -> Unit) {
        shapes[path.joinToString(LayerPathSeparator)] =
            DynamicStrokeProvider().apply(builder)
    }

    override fun ellipse(vararg path: String, builder: DynamicEllipse.() -> Unit) {
        shapes[path.joinToString(LayerPathSeparator)] =
            DynamicEllipseProvider().apply(builder)
    }

    override fun rect(vararg path: String, builder: DynamicRect.() -> Unit) {
        shapes[path.joinToString(LayerPathSeparator)] =
            DynamicRectProvider().apply(builder)
    }
}

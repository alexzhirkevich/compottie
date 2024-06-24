package io.github.alexzhirkevich.compottie.dynamic

@PublishedApi
internal class DynamicShapeLayerProvider(
    private val basePath : String? = null,
    private val root : DynamicShapeLayerProvider? = null
) : DynamicLayerProvider(), DynamicShapeLayer {

    private val nRoot get() = root ?: this

    @PublishedApi
    internal val shapes = mutableMapOf<String, DynamicShape>()

    override fun group(vararg path : String, builder: DynamicShapeLayer.() -> Unit) {
        DynamicShapeLayerProvider(
            basePath = layerPath(basePath, path.joinToString(LayerPathSeparator)),
            root = nRoot
        ).apply(builder)
    }

    override fun shape(vararg path: String, builder: DynamicShape.() -> Unit) {
        this[path.toList()] = DynamicShapeProvider().apply(builder)
    }

    override fun fill(vararg path: String, builder: DynamicFill.() -> Unit) {
        this[path.toList()] = DynamicFillProvider().apply(builder)
    }

    override fun stroke(vararg path: String, builder: DynamicStroke.() -> Unit) {
        this[path.toList()] = DynamicStrokeProvider().apply(builder)
    }

    override fun ellipse(vararg path: String, builder: DynamicEllipse.() -> Unit) {
        this[path.toList()] = DynamicEllipseProvider().apply(builder)
    }

    override fun rect(vararg path: String, builder: DynamicRect.() -> Unit) {
        this[path.toList()] = DynamicRectProvider().apply(builder)
    }

    override fun polystar(vararg path: String, builder: DynamicPolystar.() -> Unit) {
        this[path.toList()] = DynamicPolystarProvider().apply(builder)
    }

    internal inline operator fun <reified S : DynamicShape> get(path: String): S? =
        shapes[path] as? S

    private operator fun <T : DynamicShape> set(path : List<String>, instance : T){
        nRoot.shapes[layerPath(basePath, path.joinToString(LayerPathSeparator))] = instance
    }
}

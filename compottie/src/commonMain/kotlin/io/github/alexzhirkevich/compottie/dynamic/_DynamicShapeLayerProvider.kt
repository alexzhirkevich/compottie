package io.github.alexzhirkevich.compottie.dynamic

@PublishedApi
internal class DynamicShapeLayerProvider(
    private val basePath : String? = null,
    private val root : DynamicShapeLayerProvider? = null
) : DynamicLayerProvider(), DynamicLayer.Shape {

    private val nRoot get() = root ?: this

    @PublishedApi
    internal val shapes = mutableMapOf<String, DynamicShape>()

    override fun group(name: String, shape: DynamicLayer.Shape.() -> Unit) {
        DynamicShapeLayerProvider(
            basePath = layerPath(basePath, name),
            root = nRoot
        ).apply(shape)
    }

    internal inline operator fun <reified S : DynamicShape> get(path: String): S? =
        shapes[path] as? S
}

@PublishedApi
internal inline fun <reified T : DynamicStroke> DynamicLayer.Shape.strokeImpl(
    vararg path: String,
    builder: T.() -> Unit
) {
    check(this is DynamicShapeLayerProvider)

    shapes[path.joinToString(LayerPathSeparator)] = when (T::class){
        DynamicStroke.Solid::class -> (DynamicSolidStrokeProvider() as T).apply(builder)
        DynamicStroke.Gradient::class -> (DynamicGradientStrokeProvider() as T).apply(builder)
        DynamicStroke::class -> (DynamicStrokeProvider() as T).apply(builder)
        else -> error("Invalid stroke type. Must be either DynamicStroke.Solid or DynamicStroke.Gradient")
    }
}


@PublishedApi
internal inline fun <reified T : DynamicFill> DynamicLayer.Shape.fillImpl(
    vararg path: String,
    builder: T.() -> Unit
) {
    check(this is DynamicShapeLayerProvider)

    shapes[path.joinToString(LayerPathSeparator)] = when (T::class) {
        DynamicFill.Solid::class -> (DynamicSolidFillProvider() as T).apply(builder)
        DynamicFill.Gradient::class -> (DynamicGradientFillProvider() as T).apply(builder)
        DynamicFill::class -> (DynamicFillProvider() as T).apply(builder)
        else -> error("Invalid stroke type. Must be either DynamicStroke.Solid or DynamicStroke.Gradient")
    }
}
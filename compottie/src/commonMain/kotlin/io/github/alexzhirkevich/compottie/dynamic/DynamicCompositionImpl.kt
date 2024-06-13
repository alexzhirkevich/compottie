package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ScaleFactor

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


internal open class DynamicLayerProvider : DynamicLayer {

    var transform : DynamicTransformProvider? = null

    override fun transform(builder: DynamicTransform.() -> Unit) {
        transform = DynamicTransformProvider().apply(builder)
    }
}

internal class DynamicShapeLayerProvider(
    private val basePath : String? = null,
    private val root : DynamicShapeLayerProvider? = null
) : DynamicLayerProvider(), DynamicLayer.Shape {

    private val nRoot get() = root ?: this

    private val shapes = mutableMapOf<String, DynamicShape>()

    override fun group(name: String, shape: DynamicLayer.Shape.() -> Unit) {
        DynamicShapeLayerProvider(
            basePath = layerPath(basePath, name),
            root = nRoot
        ).apply(shape)
    }

    override fun fill(vararg path: String, builder: DynamicFill.() -> Unit) {
        shapes[layerPath(basePath, path.joinToString(LayerPathSeparator))] =
            DynamicFillProvider().apply(builder)
    }

    override fun stroke(vararg path: String, builder: DynamicFill.() -> Unit) {
    }

    internal fun get(path : String) : DynamicShape? = shapes[path]
}

internal inline fun <reified T : DynamicShape> DynamicShape.requireShape(path: String) : T {
    check(this is T){
        val e = T::class.simpleName!!.substringBeforeLast("Provider")
        val a = this::class.simpleName!!.substringBeforeLast("Provider")

        "$path expected to be $e but is $a"
    }

    return this
}

internal class DynamicTransformProvider : DynamicTransform {

    var scale : PropertyProvider<ScaleFactor>? = null
        private set
    var offset : PropertyProvider<Offset>? = null
        private set
    var rotation : PropertyProvider<Float>? = null
        private set
    var opacity : PropertyProvider<Float>? = null
        private set
    var skew : PropertyProvider<Float>? = null
        private set
    var skewAxis : PropertyProvider<Float>? = null
        private set

    override fun scale(provider: PropertyProvider<ScaleFactor>) {
        this.scale = provider
    }

    override fun offset(provider: PropertyProvider<Offset>) {
        this.offset = provider
    }

    override fun rotation(provider: PropertyProvider<Float>) {
        this.rotation = provider
    }

    override fun opacity(provider: PropertyProvider<Float>) {
        this.opacity = provider
    }

    override fun skew(provider: PropertyProvider<Float>) {
        this.skew = provider
    }

    override fun skewAxis(provider: PropertyProvider<Float>) {
        this.skewAxis = provider
    }
}

internal class DynamicFillProvider : DynamicFill {

    var color : PropertyProvider<Color>? = null
        private set

    var opacity : PropertyProvider<Float>? = null
        private set

    var colorFilter : PropertyProvider<ColorFilter?>? = null
        private set

    var blendMode : PropertyProvider<BlendMode>? = null
        private set

    override fun color(provider: PropertyProvider<Color>) {
        color = provider
    }

    override fun opacity(provider: PropertyProvider<Float>) {
       opacity = provider
    }

    override fun colorFilter(provider: PropertyProvider<ColorFilter?>) {
        colorFilter = provider
    }

    override fun blendMode(provider: PropertyProvider<BlendMode>) {
        blendMode = provider
    }
}
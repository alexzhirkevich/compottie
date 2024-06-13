package io.github.alexzhirkevich.compottie.dynamic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ScaleFactor

internal class DynamicCompositionImpl : DynamicComposition {

    private val layers = mutableMapOf<String, DynamicLayerImpl>()

    override fun shapeLayer(name: String, builder: DynamicLayer.Shape.() -> Unit) {
        layers[name] = DynamicShapeLayerImpl().apply(builder)
    }

    override fun layer(name: String, builder: DynamicLayer.() -> Unit) {
        layers[name] = DynamicLayerImpl().apply(builder)
    }

    operator fun get(name: String): DynamicLayerImpl? = layers[name]
}


internal open class DynamicLayerImpl : DynamicLayer {

    var transform : DynamicTransformImpl? = null

    override fun transform(builder: DynamicTransform.() -> Unit) {
        transform = DynamicTransformImpl().apply(builder)
    }
}

internal class DynamicShapeLayerImpl : DynamicLayerImpl(), DynamicLayer.Shape {

    override fun fill(name: String, builder: DynamicFill.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun stroke(name: String, builder: DynamicFill.() -> Unit) {
        TODO("Not yet implemented")
    }
}

internal class DynamicTransformImpl : DynamicTransform {

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
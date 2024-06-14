package io.github.alexzhirkevich.compottie.dynamic

internal open class DynamicLayerProvider : DynamicLayer {

    var transform : DynamicTransformProvider? = null

    override fun transform(builder: DynamicTransform.() -> Unit) {
        transform = DynamicTransformProvider().apply(builder)
    }
}
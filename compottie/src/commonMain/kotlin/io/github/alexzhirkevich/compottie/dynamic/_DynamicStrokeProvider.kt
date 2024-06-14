package io.github.alexzhirkevich.compottie.dynamic

import kotlin.js.JsName

internal interface DynamicStrokeProvider : DynamicStroke, DynamicDrawProvider

@JsName("MakeDynamicStrokeProvider")
@PublishedApi
internal fun DynamicStrokeProvider() : DynamicStrokeProvider = DynamicStrokeProviderImpl()

@PublishedApi
internal class DynamicSolidStrokeProvider : DynamicStroke.Solid,
    DynamicSolidDrawProvider by DynamicSolidDrawProvider()

@PublishedApi
internal class DynamicGradientStrokeProvider : DynamicStroke.Gradient,
    DynamicGradientDrawProvider by DynamicGradientDrawProvider()

private class DynamicStrokeProviderImpl: BaseDynamicDrawProvider(), DynamicStrokeProvider {

    var width: PropertyProvider<Float>? = null
        private set

    override fun width(provider: PropertyProvider<Float>) {
        this.width = provider
    }
}
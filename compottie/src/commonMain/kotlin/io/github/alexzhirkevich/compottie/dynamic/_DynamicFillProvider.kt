package io.github.alexzhirkevich.compottie.dynamic

import kotlin.js.JsName


internal sealed interface DynamicFillProvider : DynamicFill, DynamicDrawProvider

@PublishedApi
@JsName("MakeDynamicFillProvider")
internal fun DynamicFillProvider() : DynamicFillProvider = DynamicFillProviderImpl()

@PublishedApi
internal class DynamicSolidFillProvider : DynamicFill.Solid,
    DynamicSolidDrawProvider by DynamicSolidDrawProvider()

@PublishedApi
internal class DynamicGradientFillProvider : DynamicFill.Gradient,
    DynamicGradientDrawProvider by DynamicGradientDrawProvider()

private class DynamicFillProviderImpl : BaseDynamicDrawProvider(), DynamicFillProvider


package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.utils.degreeToRadians
import io.github.alexzhirkevich.compottie.internal.utils.radiansToDegree
import io.github.alexzhirkevich.keight.js.FunctionParam
import io.github.alexzhirkevich.keight.js.JSFunction
import io.github.alexzhirkevich.keight.js.js

internal fun JsDegreesToRadians() = JSFunction(FunctionParam("deg")) {
    degreeToRadians((it[0]?.toKotlin(this) as Number).toDouble()).js()
}

internal fun JSRadiansToDegrees() = JSFunction(FunctionParam("rad")) {
    radiansToDegree((it[0]?.toKotlin(this) as Number).toDouble()).js()
}

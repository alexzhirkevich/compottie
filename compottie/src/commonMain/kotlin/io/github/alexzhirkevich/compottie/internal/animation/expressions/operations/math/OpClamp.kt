package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.keight.js.FunctionParam
import io.github.alexzhirkevich.keight.js.JSFunction
import io.github.alexzhirkevich.keight.js.js

internal fun JsClamp() = JSFunction(
    FunctionParam("v"),
    FunctionParam("from"),
    FunctionParam("to"),
) {
    val v = it[0]?.toKotlin(this) as Number
    val from = it[1]?.toKotlin(this) as Number
    val to = it[2]?.toKotlin(this) as Number

    if (v is Long && from is Long && to is Long) {
        v.coerceIn(from, to).js()
    } else {
        v.toDouble().coerceIn(from.toDouble(), to.toDouble()).js()
    }
}
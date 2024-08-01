package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.EcmascriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.common.fastMap
import kotlin.math.absoluteValue

public open class JSRuntime : EcmascriptRuntime() {

    init {
        recreate()
    }

    override fun reset() {
        super.reset()
        recreate()
    }

    override fun isFalse(a: Any?): Boolean {
        return a == null
                || a == false
                || a is Number && a.toDouble() == 0.0
                || a is CharSequence && a.isEmpty()
                || a is Unit
                || (a as? JsNumber)?.value?.let(::isFalse) == true
    }

    override fun sum(a: Any?, b: Any?): Any? {
        return fromKotlin(
            jssum(
                fromKotlin(a)?.numberOrThis(false),
                fromKotlin(b)?.numberOrThis(false)
            )
        )
    }

    override fun sub(a: Any?, b: Any?): Any? {
        return fromKotlin(jssub(fromKotlin(a)?.numberOrThis(), fromKotlin(b)?.numberOrThis()))
    }

    override fun mul(a: Any?, b: Any?): Any? {
        return fromKotlin(jsmul(fromKotlin(a)?.numberOrThis(), fromKotlin(b)?.numberOrThis()))
    }

    override fun div(a: Any?, b: Any?): Any? {
        return fromKotlin(jsdiv(fromKotlin(a)?.numberOrThis(), fromKotlin(b)?.numberOrThis()))
    }

    override fun mod(a: Any?, b: Any?): Any? {
        return fromKotlin(jsmod(fromKotlin(a)?.numberOrThis(), fromKotlin(b)?.numberOrThis()))
    }

    override fun inc(a: Any?): Any? {
        return fromKotlin(jsinc(fromKotlin(a)?.numberOrThis()))
    }

    override fun dec(a: Any?): Any? {
        return fromKotlin(jsdec(fromKotlin(a)?.numberOrThis()))
    }

    override fun neg(a: Any?): Any? {
        return fromKotlin(jsneg(fromKotlin(a)?.numberOrThis()))
    }

    override fun pos(a: Any?): Any? {
        return fromKotlin(jspos(fromKotlin(a)?.numberOrThis()))
    }

    override fun fromKotlin(a: Any?): Any? {
        return when (a){
            is Number -> JsNumber(a)
            is UByte -> JsNumber(a.toLong())
            is UShort -> JsNumber(a.toLong())
            is UInt -> JsNumber(a.toLong())
            is ULong -> JsNumber(a.toLong())
            is List<*> -> JsArray(a.fastMap(::fromKotlin).toMutableList())
            is CharSequence -> JsString(a.toString())
            else -> a
        }
    }

    override fun toKotlin(a: Any?): Any? {
        return when(a){
            is JsNumber -> a.value
            is JsArray -> a.value.fastMap(::toKotlin)
            is JsString -> a.value
            else -> a
        }
    }

    private fun recreate(){
        variables["Math"] = VariableType.Const to JsMath()
        variables["Infinity"] = VariableType.Const to Double.POSITIVE_INFINITY
    }
}

private fun jssum(a : Any?, b : Any?) : Any? {
    val a = if (a is List<*>)
        a.joinToString(",")
    else a
    val b = if (b is List<*>)
        b.joinToString(",")
    else b
    return when {
        a == null && b == null -> 0L
        a == null && b is Number || a is Number && b == null -> a ?: b
        b is Unit || a is Unit -> Double.NaN
        a is Long && b is Long -> a + b
        a is Number && b is Number -> a.toDouble() + b.toDouble()
        else ->  a.toString() + b.toString()
    }
}

private fun jssub(a : Any?, b : Any?) : Any? {
    return when {
        a is Unit || b is Unit -> Double.NaN
        a is Number && b is Unit || a is Unit && b is Number -> Double.NaN
        a is Long? && b is Long? -> (a ?: 0L) - (b ?: 0L)
        a is Double? && b is Double? ->(a ?: 0.0) - (b ?: 0.0)
        a is Number? && b is Number? ->(a?.toDouble() ?: 0.0) - (b?.toDouble() ?: 0.0)
        else -> Double.NaN
    }
}

private fun jsmul(a : Any?, b : Any?) : Any? {
    return when {
        a == Unit || b == Unit -> Double.NaN
        a == null || b == null -> 0L
        a is Long && b is Long -> a*b
        a is Double && b is Double -> a*b
        a is Long && b is Long -> a * b
        a is Number && b is Number -> a.toDouble() * b.toDouble()
        a is List<*> && b is Number -> {
            a as List<Number>
            val bf = b.toDouble()
            a.fastMap { it.toDouble() * bf }
        }
        a is Number && b is List<*> -> {
            b as List<Number>
            val af = a.toDouble()
            b.fastMap { it.toDouble() * af }
        }
        else -> Double.NaN
    }
}

private fun jsdiv(a : Any?, b : Any?) : Any {
    return when {
        a is Unit || b is Unit
                || (a == null && b == null)
                || ((a as? Number)?.toDouble() == 0.0 && b == null)
                || ((b as? Number)?.toDouble() == 0.0 && a == null)
                || ((a as? CharSequence)?.toString()?.toDoubleOrNull() == 0.0 && b == null)
                || ((b as? CharSequence)?.toString()?.toDoubleOrNull() == 0.0 && a == null) -> Double.NaN
        a == null -> 0L
        b == null || (b as? Number)?.toDouble() == 0.0 -> Double.POSITIVE_INFINITY
        a is Long && b is Long -> when {
            a % b == 0L -> a / b
            else -> a.toDouble() / b
        }

        a is Number && b is Number -> a.toDouble() / b.toDouble()
        a is List<*> && b is Number -> {
            a as List<Number>
            val bf = b.toDouble()
            a.fastMap { it.toDouble() / bf }
        }

        else -> Double.NaN
    }
}

private fun jsmod(a : Any?, b : Any?) : Any {
    return when {
        b == null || a == Unit || b == Unit -> Double.NaN
        (b as? Number)?.toDouble()?.absoluteValue?.let { it < Double.MIN_VALUE } == true -> Double.NaN
        a == null -> 0L
        a is Long && b is Long -> a % b
        a is Number && b is Number -> a.toDouble() % b.toDouble()
        else -> Double.NaN
    }
}


private fun jsinc(v : Any?) : Any {
    return when (v) {
        null -> 1L
        is Long -> v + 1
        is Double -> v + 1
        is Number -> v.toDouble() + 1
        else -> Double.NaN
    }
}

private fun jsdec(v : Any?) : Any {
    return when (v) {
        null -> -1L
        is Long -> v - 1
        is Double -> v - 1
        is Number -> v.toDouble() - 1
        else -> Double.NaN
    }
}

private fun jsneg(v : Any?) : Any {
    return when (v) {
        null -> -0
        is Long -> -v
        is Number -> -v.toDouble()
        is List<*> -> {
            v as List<Number>
            v.fastMap { -it.toDouble() }
        }

        else -> Double.NaN
    }
}

private fun jspos(v : Any?) : Any {
    return when (v) {
        null -> 0
        is Number -> v
        else -> Double.NaN
    }
}


internal fun Any?.numberOrNull(withNaNs : Boolean = true) : Number? = when(this) {
    null -> 0
    is JsString -> if (withNaNs) value.numberOrNull(withNaNs) else null
    is JsArray -> if (withNaNs) value.numberOrNull(withNaNs) else null
    is JsWrapper<*> -> value.numberOrNull()
    is Byte -> toLong()
    is UByte -> toLong()
    is Short -> toLong()
    is UShort -> toLong()
    is Int -> toLong()
    is UInt -> toLong()
    is ULong -> toLong()
    is Float -> toDouble()
    is Long -> this
    is Double -> this
    is String -> if (withNaNs) {
        toLongOrNull() ?: toDoubleOrNull()
    } else null
    is List<*> -> {
        if (withNaNs) {
            singleOrNull()?.numberOrNull(withNaNs)
        } else{
            null
        }
    }
    else -> null
}
internal fun Any?.number(withNaNs : Boolean = true) : Number = checkNotNull(numberOrNull(withNaNs)){
    "Expected number but got $this"
}

internal fun Any.numberOrThis(withMagic : Boolean = true) : Any = numberOrNull(withMagic) ?: this

package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.LangContext
import io.github.alexzhirkevich.skriptie.common.fastMap
import kotlin.math.absoluteValue

public object JSLangContext : LangContext {

    override fun isFalse(a: Any?): Boolean {
        return a == null
                || a == false
                || a is Unit
                || a is Number && a.toDouble().let { it == 0.0 || it.isNaN() }
                || a is CharSequence && a.isEmpty()
                || (a as? JsWrapper<*>)?.value?.let(::isFalse) == true
    }

    override fun sum(a: Any?, b: Any?): Any? {
        return jssum(
            a?.numberOrThis(false),
            b?.numberOrThis(false)
        )
    }

    override fun sub(a: Any?, b: Any?): Any? {
        return jssub(a?.numberOrThis(), b?.numberOrThis())
    }

    override fun mul(a: Any?, b: Any?): Any? {
        return jsmul(a?.numberOrThis(), b?.numberOrThis())
    }

    override fun div(a: Any?, b: Any?): Any? {
        return jsdiv(a?.numberOrThis(), b?.numberOrThis())
    }

    override fun mod(a: Any?, b: Any?): Any? {
        return jsmod(a?.numberOrThis(), b?.numberOrThis())
    }

    override fun inc(a: Any?): Any? {
        return jsinc(a?.numberOrThis())
    }

    override fun dec(a: Any?): Any? {
        return jsdec(a?.numberOrThis())
    }

    override fun neg(a: Any?): Any? {
        return jsneg(a?.numberOrThis())
    }

    override fun pos(a: Any?): Any? {
        return jspos(a?.numberOrThis())
    }

    override fun toNumber(a: Any?, strict: Boolean): Number {
        return a.numberOrNull(withNaNs = !strict) ?: Double.NaN
    }

    override fun fromKotlin(a: Any?): Any? {
        return when (a) {
            is JsWrapper<*> -> a
            is Number -> JsNumber(a)
            is UByte -> JsNumber(a.toLong())
            is UShort -> JsNumber(a.toLong())
            is UInt -> JsNumber(a.toLong())
            is ULong -> {
                check(a < Long.MAX_VALUE.toULong()){
                    "Unsigned numbers grater than Long.MAX_VALUE can't be imported to JavaScript"
                }
                JsNumber(a.toLong())
            }
            is Collection<*> -> JsArray(a.map(::fromKotlin).toMutableList())
            is CharSequence -> JsString(a.toString())
            else -> a
        }
    }

    override fun toKotlin(a: Any?): Any? {
        return when (a) {
            is JsArray -> a.value.fastMap(::toKotlin)
            is JsWrapper<*> -> toKotlin(a.value)
            else -> a
        }
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


private tailrec fun Any?.numberOrNull(withNaNs : Boolean = true) : Number? = when(this) {
    null -> 0L
    true -> 1L
    false -> 0L


    is CharSequence -> when {
        isEmpty() -> 0L
        withNaNs -> {
            val s = trim().toString()
            s.toLongOrNull() ?: s.toDoubleOrNull()
        }
        else -> null
    }
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
    is List<*> -> {
        if (withNaNs) {
            singleOrNull()?.numberOrNull(withNaNs)
        } else{
            null
        }
    }
    is JsWrapper<*> -> value.numberOrNull()
    else -> null
}

private fun Any?.numberOrThis(withMagic : Boolean = true) : Any? = numberOrNull(withMagic) ?: this


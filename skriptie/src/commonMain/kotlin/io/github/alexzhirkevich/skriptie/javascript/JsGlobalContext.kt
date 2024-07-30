package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.javascript.math.JsInfinity
import io.github.alexzhirkevich.skriptie.javascript.math.JsMath
import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.GlobalContext
import io.github.alexzhirkevich.skriptie.common.fastMap
import kotlin.math.min

public open class JsGlobalContext : GlobalContext<JSScriptContext> {

    override fun interpret(
        callable: String?,
        args: List<Expression<JSScriptContext>>?
    ): Expression<JSScriptContext>? {
        return when (callable) {
            "Infinity" -> JsInfinity
            "Math" -> JsMath
            else -> null
        }
    }

    override fun sum(a: Any?, b: Any?): Any? {
        return jssum(a?.validateJsNumber(), b?.validateJsNumber())
    }

    override fun sub(a: Any?, b: Any?): Any? {
        return jssub(a?.validateJsNumber(), b?.validateJsNumber())
    }

    override fun mul(a: Any?, b: Any?): Any? {
        return jsmul(a?.validateJsNumber(), b?.validateJsNumber())
    }

    override fun div(a: Any?, b: Any?): Any? {
        return jsdiv(a?.validateJsNumber(), b?.validateJsNumber())
    }

    override fun mod(a: Any?, b: Any?): Any {
        return jsmod(a?.validateJsNumber(), b?.validateJsNumber())
    }

    override fun inc(a: Any?): Any {
        return jsinc(a?.validateJsNumber())
    }

    override fun dec(a: Any?): Any {
        return jsdec(a?.validateJsNumber())
    }

    override fun neg(a: Any?): Any {
        return jsneg(a?.validateJsNumber())
    }
}


private fun jssum(a : Any?, b : Any?) : Any? {
    return when {
        a == null && b == null -> 0L
        a == null && b is Number || a is Number && b == null -> a ?: b
        b is Unit || a is Unit -> Double.NaN
        a is Long && b is Long -> a + b
        a is Number && b is Number -> a.toDouble() + b.toDouble()
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>

            List(min(a.size, b.size)) {
                a[it].toDouble() + b[it].toDouble()
            }
        }

        a is List<*> && b is Number -> {
            if (a is MutableList<*>) {
                a as MutableList<Number>
                a[0] = a[0].toDouble() + b.toDouble()
                a
            } else {
                listOf((a as List<Number>).first().toDouble() + b.toDouble()) + a.drop(1)
            }
        }

        a is Number && b is List<*> -> {
            if (b is MutableList<*>) {
                b as MutableList<Number>
                b[0] = b[0].toDouble() + a.toDouble()
                b
            } else {
                listOf(a.toDouble() + (b as List<Number>).first().toDouble()) + b.drop(1)
            }
        }

        a is CharSequence || b is CharSequence -> a.toString() + b.toString()

        else -> error("Cant calculate the sum of $a and $b")
    }
}

private fun jssub(a : Any?, b : Any?) : Any? {
    return when {
        a is Unit || b is Unit -> Double.NaN
        a is Number && b is Unit || a is Unit && b is Number -> Double.NaN
        a is Long? && b is Long? -> (a ?: 0L) - (b ?: 0L)
        a is Double? && b is Double? ->(a ?: 0.0) - (b ?: 0.0)
        a is Number? && b is Number? ->(a?.toDouble() ?: 0.0) - (b?.toDouble() ?: 0.0)
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>
            List(min(a.size, b.size)) {
                a[it].toDouble() - b[it].toDouble()
            }
        }
        a is CharSequence || b is CharSequence -> {
            stringMath(a?.toString(), b?.toString(), Long::minus, Double::minus)
        }
        else -> error("Cant subtract $b from $a")
    }
}

private fun stringMath(
    a : String?,
    b : String?,
    long : (Long, Long) -> Long,
    double: (Double,Double) -> Double
) : Any {
    val sa = a ?: "0"
    val sb = b ?: "0"

    val la = sa.toLongOrNull()
    val lb = sb.toLongOrNull()

    return if (la != null && lb != null) {
        long(la, lb)
    } else {
        val da = sa.toDoubleOrNull()
        val db = sb.toDoubleOrNull()
        if (da != null && db != null) {
            double(da, db)
        } else {
            Double.NaN
        }
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
        a is CharSequence || b is CharSequence -> {
            stringMath(a.toString(), b.toString(), Long::times, Double::times)
        }
        else -> error("Cant multiply $a by $b")
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

        a is CharSequence || b is CharSequence -> {
            stringMath(a?.toString(), b?.toString(), Long::div, Double::div)
        }

        else -> error("Cant divide $a by $b")
    }
}

private fun jsmod(a : Any?, b : Any?) : Any {
    return when {
        a == null -> 0L
        b == null -> Double.NaN
        a is Long && b is Long -> a % b
        a is Number && b is Number -> a.toDouble() % b.toDouble()
        else -> error("Can't get mod of $a and $b")
    }
}


private fun jsinc(v : Any?) : Any {
    return when (v) {
        null -> 1L
        is Long -> v + 1
        is Double -> v + 1
        is Number -> v.toDouble() + 1
        else -> error("can't increment $v")
    }
}

private fun jsdec(v : Any?) : Any {
    return when (v) {
        null -> -1L
        is Long -> v - 1
        is Double -> v - 1
        is Number -> v.toDouble() - 1
        else -> error("can't decrement $v")
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

        else -> error("Cant apply unary minus to $v")
    }
}

internal fun Any.validateJsNumber() = when(this) {
    is Byte -> toLong()
    is UByte -> toLong()
    is Short -> toLong()
    is UShort -> toLong()
    is Int -> toLong()
    is UInt -> toLong()
    is ULong -> toLong()
    is Float -> toDouble()
    else -> this
}


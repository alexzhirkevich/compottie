package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.JsInfinity
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math.JsMath
import io.github.alexzhirkevich.skriptie.ecmascript.GlobalContext
import io.github.alexzhirkevich.skriptie.ecmascript.operations.value.fastMap
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

    override fun sum(a: Any, b: Any): Any {
        return jssum(a.validateJsNumber(), b.validateJsNumber())
    }

    override fun sub(a: Any, b: Any): Any {
        return jssub(a.validateJsNumber(), b.validateJsNumber())
    }

    override fun mul(a: Any, b: Any): Any {
        return jsmul(a.validateJsNumber(),b.validateJsNumber())
    }

    override fun div(a: Any, b: Any): Any {
        return jsdiv(a.validateJsNumber(), b.validateJsNumber())
    }

    override fun mod(a: Any, b: Any): Any {
        return jsmod(a.validateJsNumber(), b.validateJsNumber())
    }

    override fun inc(a: Any): Any {
        return jsinc(a.validateJsNumber())
    }

    override fun dec(a: Any): Any {
        return jsdec(a.validateJsNumber())
    }

    override fun neg(a: Any): Any {
        return jsneg(a.validateJsNumber())
    }
}



private fun jssum(a : Any, b : Any) : Any {
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> a+b
        a is Number && b is Number -> a.toDouble() + b.toDouble()
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>

            List(min(a.size, b.size)) {
                a[it].toDouble() + b[it].toDouble()
            }
        }

        a is List<*> && b is Number -> {
            if (a is MutableList<*>){
                a as MutableList<Number>
                a[0] = a[0].toDouble() + b.toDouble()
                a
            } else {
                listOf((a as List<Number>).first().toDouble() + b.toDouble()) + a.drop(1)
            }
        }
        a is Number && b is List<*> -> {
            if (b is MutableList<*>){
                b as MutableList<Number>
                b[0] = b[0].toDouble() + a.toDouble()
                b
            } else {
                listOf(a.toDouble() + (b as List<Number>).first().toDouble()) + b.drop(1)
            }
        }
        a is CharSequence -> a.toString() + b.toString()

        else -> error("Cant calculate the sum of $a and $b")
    }
}

private fun jssub(a : Any, b : Any) : Any {
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> a-b
        a is Double && b is Double -> a-b
        a is Number && b is Number -> a.toDouble() - b.toDouble()
        a is List<*> && b is List<*> -> {
            a as List<Number>
            b as List<Number>
            List(min(a.size, b.size)) {
                a[it].toDouble() - b[it].toDouble()
            }
        }
        a is CharSequence || b is CharSequence -> {
            a.toString().toDouble() - b.toString().toDouble()
        }
        else -> error("Cant subtract $b from $a")
    }
}

private fun jsmul(a : Any, b : Any) : Any {
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> a*b
        a is Double && b is Double -> a*b
        a is Long && b is Long -> if (b == 0) Float.POSITIVE_INFINITY else a/b
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
            a.toString().toDouble() * b.toString().toDouble()
        }
        else -> error("Cant multiply $a by $b")
    }
}

private fun jsdiv(a : Any, b : Any) : Any {
    return when {
        a is Number && b is Undefined || a is Undefined && b is Number -> Float.NaN
        a is Long && b is Long -> when {
            b == 0 -> Double.POSITIVE_INFINITY
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
            a.toString().toDouble() / b.toString().toDouble()
        }

        else -> error("Cant divide $a by $b")
    }
}

private fun jsmod(a : Any, b : Any) : Any {
    return when {
        a is Long && b is Long -> a % b
        a is Number && b is Number -> a.toDouble() % b.toDouble()
        else -> error("Can't get mod of $a and $b")
    }
}


private fun jsinc(v : Any) : Any {
    return when (v) {
        is Long -> v + 1
        is Double -> v + 1
        is Number -> v.toDouble() + 1
        else -> error("can't increment $v")
    }
}

private fun jsdec(v : Any) : Any {
    return when (v) {
        is Long -> v - 1
        is Double -> v - 1
        is Number -> v.toDouble() - 1
        else -> error("can't decrement $v")
    }
}

private fun jsneg(v : Any) : Any {
    return when (v) {
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


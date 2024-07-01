package io.github.alexzhirkevich.compottie.internal.animation.expressions

import io.github.alexzhirkevich.compottie.internal.animation.Vec2


internal object LottieOp {

    fun op(v : Operation, op : (Any) -> Any) : Operation {
        return Operation { v, vars, s ->
            op(v(v, vars, s))
        }
    }

    fun op(a : Operation, b : Operation, op : (Any, Any) -> Any) : Operation {
        return Operation { v, vars, s ->
            op(a(v, vars, s), b(v, vars, s))
        }
    }

    fun op(a : Operation, b : Operation, c : Operation, op : (Any, Any, Any) -> Any) : Operation {
        return Operation { v, vars, s ->
            op(a(v, vars, s), b(v, vars, s), c(v, vars, s))
        }
    }

    fun sum(a : Any, b : Any) : Any {
        return when {
            a is Float && b is Float -> a + b
            a is Vec2 && b is Vec2 -> a + b

            else -> error("Cant calculate the sum of $a and $b")
        }
    }

    fun index(v : Any, idx : Int?) : Any {
        if (idx == null){
            return v
        }

        return when (v) {
            is Vec2 -> when (idx) {
                0 -> v.x
                1 -> v.y
                else -> error("Cant get $idx index of Vec2")
            }

            else -> error("Cant get value by index ($idx) from $v")
        }
    }


    fun sub(a : Any, b : Any) : Any {
        TODO()
    }

    fun mul(a : Any, b : Any) : Any {
        return when {
            (a is Number && b is Number) -> a.toFloat() * b.toFloat()
            (a is Vec2 && b is Number) -> a * b.toFloat()
            (a is Number && b is Vec2) -> b * a.toFloat()
            else -> error("Cant multiply $a and $b")
        }
    }

    fun div(a : Any, b : Any) : Any {
        TODO()
    }

    fun sqrt(a : Any) : Any {
        TODO()
    }
    fun sin(a : Any) : Any {
        TODO()
    }
    fun cos(a : Any) : Any {
        require(a is Number){
            "Cant get Math.cos of $a"
        }
        return kotlin.math.cos(a.toFloat())
    }
    fun mod(a : Any, b : Any) : Any {
        TODO()
    }

    fun clamp(v : Any, from : Any, to : Any) : Any {
        require(v is Number && from is Number && to is Number) {
            "Cant clamp ($v, $from, $to) : not a number"
        }

        return v.toFloat().coerceIn(from.toFloat(), to.toFloat(),)
    }
}
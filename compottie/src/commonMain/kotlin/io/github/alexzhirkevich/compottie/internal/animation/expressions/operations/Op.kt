package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.animation.Vec2

object Op {

    fun sum(a : Any, b : Any) : Any {
        return when {
            a is Float && b is Float -> a + b
            a is Vec2 && b is Vec2 -> a + b

            else -> error("Cant calculate the sum of $a and $b")
        }
    }


    fun sub(a : Any, b : Any) : Any {
        TODO()
    }

    fun mul(a : Any, b : Any) : Any {
        TODO()
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
        TODO()
    }
    fun mod(a : Any, b : Any) : Any {
        TODO()
    }
}
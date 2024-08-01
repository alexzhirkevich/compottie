package io.github.alexzhirkevich.skriptie

import io.github.alexzhirkevich.skriptie.javascript.JSRuntime
import io.github.alexzhirkevich.skriptie.javascript.number
import kotlin.math.min

public class ExpressionRuntime : JSRuntime() {

    override fun sum(a: Any?, b: Any?): Any? {
        return when {
            a is List<*> && b is List<*> -> {

                List(min(a.size, b.size)) {
                    a[it].number().toDouble() + b[it].number().toDouble()
                }
            }

            a is List<*> && b is Number -> {
                if (a is MutableList<*>) {
                    a as MutableList<Any?>
                    a[0] = a[0].number().toDouble() + b.toDouble()
                    a
                } else {
                    listOf((a.first().number().toDouble() + b.toDouble()) )+ a.drop(1)
                }
            }

            a is Number && b is List<*> -> {
                if (b is MutableList<*>) {
                    b as MutableList<Any?>
                    b[0] = b[0].number().toDouble() + a.toDouble()
                    b
                } else {
                    listOf(a.toDouble() + (b).first().number().toDouble()) + b.drop(1)
                }
            }

            else -> super.sum(a, b)
        }
    }

    override fun sub(a: Any?, b: Any?): Any? {
        return when  {
            a is List<*> && b is List<*> -> {
                a as List<Number>
                b as List<Number>
                List(min(a.size, b.size)) {
                    a[it].toDouble() - b[it].toDouble()
                }
            }
            else -> super.sub(a, b)
        }
    }
}
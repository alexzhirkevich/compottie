package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.argAt
import io.github.alexzhirkevich.skriptie.common.Callable
import io.github.alexzhirkevich.skriptie.common.OpConstant
import io.github.alexzhirkevich.skriptie.common.TypeError
import io.github.alexzhirkevich.skriptie.common.checkNotEmpty
import io.github.alexzhirkevich.skriptie.common.fastFilter
import io.github.alexzhirkevich.skriptie.common.fastMap
import io.github.alexzhirkevich.skriptie.common.valueAtIndexOrUnit
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgs
import io.github.alexzhirkevich.skriptie.invoke
import kotlin.jvm.JvmInline

@JvmInline
internal value class JsArray(
    override val value : MutableList<Any?>
) : ESAny, JsWrapper<MutableList<Any?>>, MutableList<Any?> by value {

    override fun get(variable: Any?): Any {
        return when (variable){
            "length" -> value.size
            else -> Unit
        }
    }

    override fun toString(): String {
        return value.joinToString(separator = ",")
    }

    override fun invoke(
        function: String,
        context: ScriptRuntime,
        arguments: List<Expression>
    ): Any? {
        return when (function){
            "indexOf" -> value.indexOf(false,function, context, arguments)
            "lastIndexOf" -> value.indexOf(true,function, context, arguments)
            "forEach" -> {
                op(context, arguments, function) { callable ->
                    value.forEach {
                        callable.invoke(listOf(OpConstant(it)), context)
                    }
                }
            }
            "map" -> {
                op(context, arguments, function) { callable ->
                    value.fastMap {
                        callable.invoke(listOf(OpConstant(it)), context)
                    }
                }
            }
            "filter" -> {
                op(context, arguments, function) { callable ->
                    value.fastFilter {
                       !context.isFalse(callable.invoke(listOf(OpConstant(it)), context))
                    }
                }
            }
            "reduce" -> {
                val v = if (arguments.size > 1) {
                    listOf(arguments[1].invoke(context)) + value
                } else {
                    value
                }
                op(context, arguments, function) { callable ->
                    v.reduce { acc, any ->
                        callable.invoke(
                            listOf(
                                OpConstant(acc),
                                OpConstant(any),
                            ),
                            context
                        )
                    }
                }
            }
            "reduceRight" -> {
                val v = if (arguments.size > 1) {
                    value + arguments[1].invoke(context)
                } else {
                    value
                }
                op(context, arguments, function) { callable ->
                    v.reduceRight { acc, any ->
                        callable.invoke(
                            listOf(
                                OpConstant(acc),
                                OpConstant(any),
                            ),
                            context
                        )
                    }
                }
            }
            "some" -> {
                op(context, arguments, function) { callable ->
                    value.any {
                        !context.isFalse(callable.invoke(listOf(OpConstant(it)), context))
                    }
                }
            }
            "reverse" -> value.reverse()
            "toReversed" -> JsArray(value.reversed().toMutableList())
            "sort" -> {
                value.sortWith(context.comparator)
                this
            }
            "toSorted" -> JsArray(value.sortedWith(context.comparator).toMutableList())
            "slice" -> {
                if (arguments.isEmpty()){
                    this
                } else {
                    val start = arguments[0].invoke(context).let(context::toNumber).toInt()
                    val end = if (arguments.size < 2)
                        value.size
                    else
                        arguments[1].invoke(context).let(context::toNumber).toInt().coerceIn(0, value.size)

                    value.slice(start..<end)
                }
            }
            "at" -> {
                val idx = arguments[0].invoke(context).let(context::toNumber).toInt()
                value.valueAtIndexOrUnit(idx)
            }
            "includes" -> {
                val v = arguments[0].invoke(context)
                val fromIndex = arguments.getOrNull(1)?.let(context::toNumber)?.toInt() ?: 0
                value.indexOf(v) > fromIndex
            }
            else -> super.invoke(function, context, arguments)
        }
    }
}

private fun <R> op(
    runtime : ScriptRuntime,
    arguments: List<Expression>,
    function: String,
    op : (Callable) -> R
) : R {
    val func = arguments[0].invoke(runtime)
    if (func !is Callable){
        throw TypeError("$func is not a function")
    }
    return op(func)
}

private fun List<*>.indexOf(
    last : Boolean,
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Any {
    checkArgs(arguments, 1, function)
    val search = checkNotEmpty(arguments.argAt(0).invoke(context))

    return if (last)
        lastIndexOf(search)
    else indexOf(search)
}

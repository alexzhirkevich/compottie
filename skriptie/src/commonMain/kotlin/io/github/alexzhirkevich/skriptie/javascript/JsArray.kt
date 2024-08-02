package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.argAt
import io.github.alexzhirkevich.skriptie.common.checkNotEmpty
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgs
import io.github.alexzhirkevich.skriptie.invoke
import kotlin.jvm.JvmInline

@JvmInline
internal value class JsArray(
    override val value : MutableList<Any?>
) : MutableList<Any?> by value, ESAny, JsWrapper<MutableList<Any?>> {

//    override fun compareTo(other: JsArray): Int {
//        repeat(min(v.size, other.v.size)) {
//            val a = v[it]
//            val b = other.v[it]
//            if (a is Comparable<*> && b is Comparable<*>) {
//                a.compareTo(b)
//            }
//        }
//    }

    override fun get(property: String): Any {
        return when (property){
            "length" -> value.size
            else -> Unit
        }
    }

    override fun invoke(
        function: String,
        context: ScriptRuntime,
        arguments: List<Expression>
    ): Any? {
        return when (function){
            "indexOf" -> value.indexOf(false,function, context, arguments)
            "lastIndexOf" -> value.indexOf(true,function, context, arguments)
            else -> super.invoke(function, context, arguments)
        }
    }
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

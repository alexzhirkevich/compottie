package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.argAt
import io.github.alexzhirkevich.skriptie.argAtOrNull
import io.github.alexzhirkevich.skriptie.common.checkNotEmpty
import io.github.alexzhirkevich.skriptie.common.valueAtIndexOrUnit
import io.github.alexzhirkevich.skriptie.ecmascript.ESAny
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgs
import io.github.alexzhirkevich.skriptie.ecmascript.checkArgsNotNull
import io.github.alexzhirkevich.skriptie.invoke
import kotlin.jvm.JvmInline

@JvmInline
internal value class JsString(
    override val value : String
) : ESAny, JsWrapper<String>, Comparable<JsString> {

    override val type: String
        get() = "string"


    override fun toString(): String {
        return value
    }

    override fun get(property: String): Any {
        return when(property){
            "length" -> value.length
            else -> Unit
        }
    }

    override fun invoke(
        function: String,
        context: ScriptRuntime,
        arguments: List<Expression>
    ): Any? {
        return when (function) {
            "charAt", "at" -> value.charAt(function, context, arguments)
            "indexOf", -> value.indexOf(false,function, context, arguments)
            "lastIndexOf", -> value.indexOf(true, function, context, arguments)
            "charCodeAt" -> value.charCodeAt(function, context, arguments)
            "endsWith" -> value.endsWith(function, context, arguments)
            "startsWith" -> value.startsWith(function, context, arguments)
            "includes" -> value.includes(function, context, arguments)
            "padStart" -> value.padStart(function, context, arguments)
            "padEnd" -> value.padEnd(function, context, arguments)
            "match" -> value.match(function, context, arguments)
            "replace" -> value.replace(false, function, context, arguments)
            "replaceAll" -> value.replace(true, function, context, arguments)
            "repeat" -> value.repeat(function, context, arguments)
            "trim" -> value.trim()
            "trimStart" -> value.trimStart()
            "trimEnd" -> value.trimEnd()
            "substring", "substr" -> value.substring(function, context, arguments)
            "toUppercase", "toLocaleLowerCase" -> value.uppercase()
            "toLowerCase", "toLocaleUppercase" -> value.uppercase()
            else -> super.invoke(function, context, arguments)
        }
    }

    override fun compareTo(other: JsString): Int {
        return value.compareTo(other.value)
    }
}

private fun String.charAt(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Any {
    checkArgs(arguments, 1, function)
    val idx = (arguments.argAt(0).invoke(context).number()).toInt()
    return valueAtIndexOrUnit(idx)
}

private fun String.indexOf(
    last : Boolean,
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Any {
    checkArgs(arguments, 1, function)
    val search = checkNotEmpty(arguments.argAt(0).invoke(context).toString()[0])

    return if (last)
        lastIndexOf(search)
    else indexOf(search)
}

private fun String.charCodeAt(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Int {
    checkArgs(arguments, 1, function)
    val ind = arguments.argAt(0).invoke(context).number().toInt()
    return get(ind).code
}

private fun String.endsWith(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Boolean {
    checkArgsNotNull(arguments, function)
    val searchString = arguments.argAt(0).invoke(context).toString()
    val position = arguments.argAtOrNull(1)?.invoke(context)?.number()?.toInt()
    return if (position == null) {
        endsWith(searchString)
    } else {
        take(position.toInt()).endsWith(searchString)
    }
}

private fun String.startsWith(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Boolean {
    val searchString = arguments.argAt(0).invoke(context).toString()
    val position = arguments.argAtOrNull(1)?.number()?.toInt()
    return if (position == null) {
        startsWith(searchString)
    } else {
        drop(position.toInt()).startsWith(searchString)
    }
}


private fun String.includes(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Boolean {
    val searchString = arguments.argAt(0).invoke(context).toString()
    val position = arguments.argAtOrNull(1)?.invoke(context)?.number()?.toInt()

    return if (position == null) {
        contains(searchString)
    } else {
        drop(position.toInt()).contains(searchString)
    }
}
private fun String.padStart(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): String {
    val targetLength = arguments.argAt(0).invoke(context).number().toInt()
    val padString = arguments.argAtOrNull(1)?.invoke(context)?.toString() ?: " "
    val toAppend = targetLength - length

    return buildString(targetLength) {
        while (length < toAppend) {
            append(padString.take(toAppend - length))
        }
        append(this@padStart)
    }
}
private fun String.padEnd(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): String {
    val targetLength = arguments.argAt(0).invoke(context).number().toInt()
    val padString = arguments.argAtOrNull(1)?.invoke(context)?.toString() ?: " "

    return buildString(targetLength) {
        append(this@padEnd)
        while (length < targetLength) {
            append(padString.take(targetLength - length))
        }
    }
}

private fun String.match(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): Boolean {
    checkArgs(arguments, 1, function)
    val regex = arguments.argAt(0).invoke(context).toString()
    return matches(regex.toRegex())
}

private fun String.replace(
    all : Boolean,
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): String {
    checkArgs(arguments, 2, function)
    val pattern = arguments.argAt(0).invoke(context).toString()
    val replacement = arguments.argAt(0).invoke(context).toString()

    return when {
        pattern.isEmpty() -> replacement + this
        all -> replace(pattern, replacement)
        else -> replaceFirst(pattern, replacement)
    }
}

private fun String.repeat(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): String {
    checkArgs(arguments, 1, function)
    val count = arguments.argAt(0).invoke(context).number().toInt()
    return repeat(count)
}

private fun String.substring(
    function: String,
    context: ScriptRuntime,
    arguments: List<Expression>
): String {
    val start = arguments.get(0).invoke(context).number().toInt()
    val end = arguments.get(0).invoke(context)?.number()?.toInt()?.coerceAtMost(length) ?: length
    return substring(start, end)
}
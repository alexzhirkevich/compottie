package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext

internal fun JsEndsWith(
    string : Expression<JSScriptContext>,
    searchString : Expression<JSScriptContext>,
    position : Expression<JSScriptContext>?
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val searchString = searchString(context) as String
    val position = position?.invoke(context) as? Number?

    if (position == null){
        string.endsWith(searchString)
    } else {
        string.take(position.toInt()).endsWith(searchString)
    }
}

internal fun JsIncludes(
    string : Expression<JSScriptContext>,
    searchString : Expression<JSScriptContext>,
    position : Expression<JSScriptContext>?
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val searchString = searchString(context) as String
    val position = position?.invoke(context) as? Number?

    if (position == null){
        string.contains(searchString)
    } else {
        string.drop(position.toInt()).contains(searchString)
    }
}

internal fun JsMatch(
    string : Expression<JSScriptContext>,
    regexp : Expression<JSScriptContext>,
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val regex = regexp(context) as String

    string.matches(regex.toRegex())
}

internal fun JsPadEnd(
    string : Expression<JSScriptContext>,
    targetLength : Expression<JSScriptContext>,
    padString : Expression<JSScriptContext>?,
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val padString = padString?.invoke(context) as String? ?: " "
    val targetLength = (targetLength(context) as Number).toInt()

    buildString(targetLength) {
        append(string)
        while (length < targetLength) {
            append(padString.take(targetLength - length))
        }
    }
}


internal fun JsPadStart(
    string : Expression<JSScriptContext>,
    targetLength : Expression<JSScriptContext>,
    padString : Expression<JSScriptContext>?,
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val padString = padString?.invoke(context) as String? ?: " "
    val targetLength = (targetLength(context) as Number).toInt()

    val toAppend = targetLength - string.length

    buildString(targetLength) {
        while (length < toAppend) {
            append(padString.take(toAppend - length))
        }
        append(string)
    }
}

internal fun JsRepeat(
    string : Expression<JSScriptContext>,
    count : Expression<JSScriptContext>,
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val count = (count(context) as Number).toInt()
    string.repeat(count)
}

internal fun JsReplace(
    string : Expression<JSScriptContext>,
    pattern : Expression<JSScriptContext>,
    replacement : Expression<JSScriptContext>,
    all : Boolean
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val pattern = pattern(context) as String
    val replacement = replacement(context) as String

    if (pattern.isEmpty()) {
        replacement + string
    } else {
        if (all) {
            string.replace(pattern, replacement)
        } else {
            string.replaceFirst(pattern, replacement)
        }
    }
}

internal fun JsStartsWith(
    string : Expression<JSScriptContext>,
    searchString : Expression<JSScriptContext>,
    position : Expression<JSScriptContext>?
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val searchString = searchString(context) as String
    val position = position?.invoke(context) as? Number?

    if (position == null){
        string.startsWith(searchString)
    } else {
        string.drop(position.toInt()).startsWith(searchString)
    }
}


internal fun JsSubstring(
    string : Expression<JSScriptContext>,
    start : Expression<JSScriptContext>,
    end : Expression<JSScriptContext>?
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String
    val start = (start(context) as Number).toInt()
    val end = (end?.invoke(context) as? Number?)?.toInt()
        ?.coerceAtMost(string.length) ?: string.length

    string.substring(start, end)
}

internal fun JsTrim(
    string : Expression<JSScriptContext>,
    start : Boolean,
    end : Boolean,
) = Expression<JSScriptContext> { context ->
    val string = string(context) as String

    when {
        start && end -> string.trim()
        start -> string.trimStart()
        end -> string.trimEnd()
        else -> string
    }
}
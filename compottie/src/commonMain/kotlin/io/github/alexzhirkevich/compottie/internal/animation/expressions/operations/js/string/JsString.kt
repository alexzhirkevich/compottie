package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun JsEndsWith(
    string : Expression,
    searchString : Expression,
    position : Expression?
) = Expression { property, context, state ->
    val string = string(property,context,state) as String
    val searchString = searchString(property, context, state) as String
    val position = position?.invoke(property, context, state) as? Number?

    if (position == null){
        string.endsWith(searchString)
    } else {
        string.take(position.toInt()).endsWith(searchString)
    }
}

internal fun JsIncludes(
    string : Expression,
    searchString : Expression,
    position : Expression?
) = Expression { property, context, state ->
    val string = string(property,context,state) as String
    val searchString = searchString(property, context, state) as String
    val position = position?.invoke(property, context, state) as? Number?

    if (position == null){
        string.contains(searchString)
    } else {
        string.drop(position.toInt()).contains(searchString)
    }
}

internal fun JsMatch(
    string : Expression,
    regexp : Expression,
) = Expression { property, context, state ->
    val string = string(property, context, state) as String
    val regex = regexp(property, context, state) as String

    string.matches(regex.toRegex())
}

internal fun JsPadEnd(
    string : Expression,
    targetLength : Expression,
    padString : Expression?,
) = Expression { property, context, state ->
    val string = string(property, context, state) as String
    val padString = padString?.invoke(property, context, state) as String? ?: " "
    val targetLength = (targetLength(property, context, state) as Number).toInt()

    buildString(targetLength) {
        append(string)
        while (length < targetLength) {
            append(padString.take(targetLength - length))
        }
    }
}


internal fun JsPadStart(
    string : Expression,
    targetLength : Expression,
    padString : Expression?,
) = Expression { property, context, state ->
    val string = string(property, context, state) as String
    val padString = padString?.invoke(property, context, state) as String? ?: " "
    val targetLength = (targetLength(property, context, state) as Number).toInt()

    val toAppend = targetLength - string.length

    buildString(targetLength) {
        while (length < toAppend) {
            append(padString.take(toAppend - length))
        }
        append(string)
    }
}

internal fun JsRepeat(
    string : Expression,
    count : Expression,
) = Expression { property, context, state ->
    val string = string(property, context, state) as String
    val count = (count(property, context, state) as Number).toInt()
    string.repeat(count)
}

internal fun JsReplace(
    string : Expression,
    pattern : Expression,
    replacement : Expression,
    all : Boolean
) = Expression { property, context, state ->
    val string = string(property, context, state) as String
    val pattern = pattern(property, context, state) as String
    val replacement = replacement(property, context, state) as String

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
    string : Expression,
    searchString : Expression,
    position : Expression?
) = Expression { property, context, state ->
    val string = string(property,context,state) as String
    val searchString = searchString(property, context, state) as String
    val position = position?.invoke(property, context, state) as? Number?

    if (position == null){
        string.startsWith(searchString)
    } else {
        string.drop(position.toInt()).startsWith(searchString)
    }
}


internal fun JsSubstring(
    string : Expression,
    start : Expression,
    end : Expression?
) = Expression { property, context, state ->
    val string = string(property,context,state) as String
    val start = (start(property, context, state) as Number).toInt()
    val end = (end?.invoke(property, context, state) as? Number?)?.toInt()
        ?.coerceAtMost(string.length) ?: string.length

    string.substring(start, end)
}

internal fun JsTrim(
    string : Expression,
    start : Boolean,
    end : Boolean,
) = Expression { property, context, state ->
    val string = string(property, context, state) as String

    when {
        start && end -> string.trim()
        start -> string.trimStart()
        end -> string.trimEnd()
        else -> string
    }
}
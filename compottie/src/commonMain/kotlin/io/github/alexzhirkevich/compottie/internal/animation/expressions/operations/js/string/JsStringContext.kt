package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.string

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argAt
import io.github.alexzhirkevich.compottie.internal.animation.expressions.argForNameOrIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgsNotNull
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.cast
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.js.JsContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value.OpIndex
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.withCast

internal object JsStringContext : JsContext {

    override fun interpret(parent: Expression, op: String?, args: List<Expression>?): Expression? {
        return when (op) {
            "charAt", "at" -> {
                checkArgs(args, 1, op)
                OpIndex(parent, args.argAt(0))
            }

            "charCodeAt" -> {
                checkArgs(args, 1, op)
                val ind = args.argAt(0)

                parent.withCast<String, Int> { property, context, state ->
                    val index = (ind(property, context, state) as Number).toInt()
                    get(index).code
                }
            }

            "endsWith" -> {
                checkArgsNotNull(args, op)
                JsEndsWith(
                    string = parent,
                    searchString = args.argForNameOrIndex(0, "searchString")!!,
                    position = args.argForNameOrIndex(1, "endPosition")
                )
            }

            "startsWith" -> {
                checkArgsNotNull(args, op)
                JsStartsWith(
                    string = parent,
                    searchString = args.argForNameOrIndex(0, "searchString")!!,
                    position = args.argForNameOrIndex(1, "position")
                )
            }

            "includes" -> {
                checkArgsNotNull(args, op)
                JsIncludes(
                    string = parent,
                    searchString = args.argForNameOrIndex(0, "searchString")!!,
                    position = args.argForNameOrIndex(1, "position")
                )
            }

            "padStart" -> {
                checkArgsNotNull(args, op)
                JsPadStart(
                    string = parent,
                    targetLength = args.argForNameOrIndex(0, "targetLength")!!,
                    padString = args.argForNameOrIndex(1, "padString")
                )
            }

            "padEnd" -> {
                checkArgsNotNull(args, op)
                JsPadEnd(
                    string = parent,
                    targetLength = args.argForNameOrIndex(0, "targetLength")!!,
                    padString = args.argForNameOrIndex(1, "padString")
                )
            }

            "match" -> {
                checkArgsNotNull(args, op)
                JsMatch(
                    string = parent,
                    regexp = args.argAt(0)
                )
            }

            "replace", "replaceAll" -> {
                checkArgsNotNull(args, op)
                JsReplace(
                    string = parent,
                    pattern = args.argForNameOrIndex(0, "pattern")!!,
                    replacement = args.argForNameOrIndex(1, "replacement")!!,
                    all = op.endsWith("All")
                )
            }

            "repeat" -> {
                checkArgsNotNull(args, op)
                JsRepeat(
                    string = parent,
                    count = args.argAt(0)
                )
            }

            "trim", "trimStart", "trimEnd" -> {
                JsTrim(
                    string = parent,
                    start = op != "trimEnd",
                    end = op != "trimStart"
                )
            }

            "substring", "substr" -> {
                checkArgsNotNull(args, op)
                JsSubstring(
                    string = parent,
                    start = args.argForNameOrIndex(0, "start", "indexStart")!!,
                    end = args.argForNameOrIndex(1, "end", "indexEnd")
                )
            }

            "toUppercase", "toLocaleLowerCase" -> {
                parent.cast(String::uppercase)
            }
            "toLowerCase", "toLocaleUppercase" -> {
                parent.cast(String::lowercase)
            }
            else -> {
                null
            }
        }
    }
}

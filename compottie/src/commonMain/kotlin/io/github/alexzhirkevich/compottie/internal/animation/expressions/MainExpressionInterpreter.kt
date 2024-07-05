package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.Compottie

internal class MainExpressionInterpreter(expr : String) : ExpressionInterpreter {

    private val expressions = try {
        val lines = expr
            .replace("\t", " ")
            .replace("\r", "")
            .split(";", "\n")
            .filter(String::isNotBlank)

        buildList {
            var i = 0

            while (i < lines.size) {

                var line = lines[i]

                while (line.endsWithExpressionChar() || line.countOpenedBlocks()> 0) {

                    check(i < lines.lastIndex) {
                        "Unexpected end of line: $line"
                    }
                    line += ";" + lines[i + 1]
                    i++
                }

                add(line)
                i++
            }
        }.map {
            try {
                SingleExpressionInterpreter(it).interpret()
            } catch (t: Throwable) {
                Compottie.logger?.warn(
                    "Unsupported or invalid Lottie expression: $it. You can ignore it if animation runs fine or expressions are disabled (${t.message})"
                )
//                    Compottie.logger?.error(
//                        "Unsupported or invalid Lottie expression: $it. You can ignore it if animation runs fine or expressions are disabled",
//                        t
//                    )

                throw t
            }
        }
    } catch (t: Throwable) {
        emptyList()
    }

    override fun interpret(): Expression {
        return Expression { property, variables, state ->
            expressions.fastForEach {
                it(property, variables, state)
            }
        }
    }
}

private val expressionChars = setOf('=', '+', '-', '*', '/')
private fun String.endsWithExpressionChar() = last { it != ' ' } in expressionChars
private fun String.countOpenedBlocks() : Int {
    var inString : Char? = null
    var openedBlocks = 0
    forEach {
        when {
            inString == null && it == '\'' || it == '"' -> {
                inString = it
            }
            it == inString -> {
                inString = null
            }
            it == '{' -> {
                openedBlocks++
            }
            it == '}' -> {
                openedBlocks--
            }
        }
    }
    return openedBlocks
}

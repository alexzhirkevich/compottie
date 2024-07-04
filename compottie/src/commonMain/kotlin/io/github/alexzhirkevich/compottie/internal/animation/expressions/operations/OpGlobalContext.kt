package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.PropertyAnimation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs

internal object OpGlobalContext : ExpressionContext<Nothing>, Expression{

    override fun parse(op: String, args: List<Expression>): Expression {
        return when (op) {
            "Math" -> OpMath
            "time" -> OpGetTime
            "value" -> OpPropertyValue()
            "thisComp" -> {
                if (args.isEmpty()) {
                    OpGetComp(null)
                } else {
                    OpGetLayer(name = args.single())
                }
            }
            "comp" -> {
                checkArgs(args, 1, op)
                return OpGetComp(args[0])
            }
            "thisLayer" -> OpGetLayer()
            "thisProperty" -> OpGetProperty()
            "add","\$bm_sum", "sum" -> {
                checkArgs(args, 2, op)
                OpAdd(args[0], args[1])
            }

            "sub","\$bm_sub" -> {
                checkArgs(args, 2, op)
                OpSub(args[0], args[1])
            }

            "mul","\$bm_mul" -> {
                checkArgs(args, 2, op)
                OpMul(args[0], args[1])
            }

            "div","\$bm_div" -> {
                checkArgs(args, 2, op)
                OpDiv(args[0], args[1])
            }

            "mod" -> {
                checkArgs(args, 2, op)
                OpMod(args[0], args[1])
            }

            "clamp" -> {
                checkArgs(args, 3, op)
                OpClamp(args[0], args[1], args[2])
            }

            "if", "else" -> error("Compottie doesn't support conditions in expressions yet")

            else -> {
                require(args.isEmpty()) {
                    "Unknown function: $op"
                }
                OpGetVariable(op)
            }
        }
    }

    override fun invoke(
        property: PropertyAnimation<Any>,
        variables: MutableMap<String, Any>,
        state: AnimationState
    ): Any {
        return Undefined
    }
}
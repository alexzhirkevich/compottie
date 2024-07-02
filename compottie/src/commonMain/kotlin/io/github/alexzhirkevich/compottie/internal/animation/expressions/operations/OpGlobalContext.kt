package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OperationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs

internal object OpGlobalContext : OperationContext, Operation{

    override fun evaluate(op: String, args: List<Operation>): Operation {
        return when (op) {
            "Math" -> OpMath
            "time" -> OpGetTime
            "value" -> OpGetValue
            "thisComp" -> {
                if (args.isEmpty()) {
                    OpGetComp(null)
                } else {
                    OpGetLayer(args.single())
                }
            }
            "comp" -> {
                checkArgs(args, 1, op)

                return OpGetComp(args[0])
            }
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

            else -> {
                require(args.isEmpty()) {
                    "Unknown function: $op"
                }
                OpGetVariable(op)
            }
        }
    }

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return Undefined
    }
}
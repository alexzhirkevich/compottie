package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Operation
import io.github.alexzhirkevich.compottie.internal.animation.expressions.OperationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.utils.Math

internal object OpMath : Operation, OperationContext {

    override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
        return Math
    }

    override fun evaluate(
        op : String,
        args : List<Operation>
    ) : Operation {
        return when(op){
            "PI" -> OpGetPI
            "cos" -> {
                checkArgs(args, 1, op)
                Cos(args[0])
            }
            "sin" -> {
                checkArgs(args, 1, op)
                Sin(args[0])
            }
            "sqrt" -> {
                checkArgs(args, 1, op)
                Sqrt(args[0])
            }
            else -> error("Unsupported Math operation: $op")
        }
    }


    class Cos(val source : Operation) : Operation {
        override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
            val a = source(value, variables, state)
            require(a is Number){
                "Can't get Math.cos of $a"
            }
            return kotlin.math.cos(a.toFloat())
        }
    }

    class Sin(val source : Operation) : Operation {
        override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
            val a = source(value, variables, state)
            require(a is Number){
                "Can't get Math.sin of $a"
            }
            return kotlin.math.sin(a.toFloat())
        }
    }

    class Sqrt(val source : Operation) : Operation {
        override fun invoke(value: Any, variables: MutableMap<String, Any>, state: AnimationState): Any {
            val a = source(value, variables, state)
            require(a is Number){
                "Can't get Math.sqrt of $a"
            }
            return kotlin.math.sqrt(a.toFloat())
        }
    }
}
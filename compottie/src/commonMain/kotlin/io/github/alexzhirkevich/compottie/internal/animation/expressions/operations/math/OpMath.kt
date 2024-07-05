package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.math

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.checkArgs
import io.github.alexzhirkevich.compottie.internal.utils.Math

internal object OpMath : Expression, ExpressionContext<Math> {

    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
        return Math
    }

    override fun interpret(
        op: String,
        args: List<Expression>
    ): Expression {
        return when (op) {
            "PI" -> PI
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

            "tan" -> {
                checkArgs(args, 1, op)
                Sqrt(args[0])
            }

            else -> error("Unsupported Math operation: $op")
        }
    }


    class Cos(val source: Expression) : Expression {
        override fun invoke(
            property: RawProperty<Any>,
            context: EvaluationContext,
            state: AnimationState
        ): Float {
            val a = source(property, context, state)
            require(a is Number) {
                "Can't get Math.cos of $a"
            }
            return kotlin.math.cos(a.toFloat())
        }
    }

    class Sin(val source: Expression) : Expression {

        override fun invoke(
            property: RawProperty<Any>,
            context: EvaluationContext, state: AnimationState
        ): Float {
            val a = source(property, context, state)
            require(a is Number) {
                "Can't get Math.sin of $a"
            }
            return kotlin.math.sin(a.toFloat())
        }
    }

    class Sqrt(val source: Expression) : Expression {

        override fun invoke(
            property: RawProperty<Any>,
            context: EvaluationContext,
            state: AnimationState
        ): Float {
            val a = source(property, context, state)
            require(a is Number) {
                "Can't get Math.sqrt of $a"
            }
            return kotlin.math.sqrt(a.toFloat())
        }
    }

    internal object PI : Expression {

        private const val floatPI = kotlin.math.PI.toFloat()

        override fun invoke(
            property: RawProperty<Any>,
            context: EvaluationContext,
            state: AnimationState
        ): Float {
            return floatPI
        }
    }
}
package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords

import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpNot(
    condition : Expression
) = Expression { property, context, state ->
    !(condition(property, context, state) as Boolean)
}

internal class OpBooleanAnd(
    private val a : Expression,
    private val b : Expression,
) : Expression {
    override fun invoke(
        property: RawProperty<Any>,
        context: EvaluationContext,
        state: AnimationState
    ): Any {
       return !a(property, context, state).isFalse() && !b(property, context, state).isFalse()
    }
}

internal fun OpBoolean(
    a : Expression,
    b : Expression,
    op : (Boolean, Boolean) -> Boolean,
    name: String,
) = Expression { property, context, state ->

    println(name)
    op(!a(property, context, state).isFalse(), !b(property, context, state).isFalse())
}


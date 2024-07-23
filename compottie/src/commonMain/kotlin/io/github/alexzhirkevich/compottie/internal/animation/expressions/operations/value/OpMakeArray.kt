package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression

internal fun OpMakeArray(
    items : List<Expression>
) = Expression { property, context, state ->
    items.fastMap { it.invoke(property, context, state) }
}
package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.value

import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.keywords.OpEqualsImpl

internal fun OpCompare(
    a : Expression,
    b : Expression,
    comparator : (Comparable<*>, Comparable<*>) -> Any
) = Expression { property, context, state ->

    comparator(
        a(property, context, state) as Comparable<*>,
        b(property, context, state) as Comparable<*>
    )
}

internal val OpGreaterComparator : (Comparable<*>, Comparable<*>) -> Boolean = { a, b ->
    if (a is Number && b is Number) {
        a.toDouble() > b.toDouble()
    } else {
        a.toString() > b.toString()
    }
}

internal val OpLessComparator : (Comparable<*>, Comparable<*>) -> Boolean = { a, b ->
    if (a is Number && b is Number) {
        a.toDouble() < b.toDouble()
    } else {
        a.toString() < b.toString()
    }
}

internal val OpTypedEqualsComparator : (Comparable<*>, Comparable<*>) -> Boolean = { a, b ->
    OpEqualsImpl(a, b, true)
}

internal val OpEqualsComparator : (Comparable<*>, Comparable<*>) -> Boolean = { a, b ->
    OpEqualsImpl(a, b, false)
}

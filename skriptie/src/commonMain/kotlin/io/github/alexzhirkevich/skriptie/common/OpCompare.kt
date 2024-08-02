package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpCompare(
    a : Expression,
    b : Expression,
    comparator : (Comparable<*>, Comparable<*>) -> Any
) = Expression {
    comparator(
        a(it) as Comparable<*>,
        b(it) as Comparable<*>
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

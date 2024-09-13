package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.invoke

internal fun  OpCompare(
    a : Expression,
    b : Expression,
    comparator : (Comparable<*>, Comparable<*>, ScriptRuntime) -> Any
) = Expression {
    comparator(
        a(it) as Comparable<*>,
        b(it) as Comparable<*>,
        it
    )
}



internal val OpGreaterComparator : (Comparable<*>, Comparable<*>, ScriptRuntime) -> Boolean = { a, b, _ ->

    if (a is Number && b is Number) {
        a.toDouble() > b.toDouble()
    } else {
        a.toString() > b.toString()
    }
}

internal val OpLessComparator : (Comparable<*>, Comparable<*>, ScriptRuntime) -> Boolean = { a, b, _ ->
    if (a is Number && b is Number) {
        a.toDouble() < b.toDouble()
    } else {
        a.toString() < b.toString()
    }
}

internal val OpTypedEqualsComparator : (Comparable<*>, Comparable<*>, ScriptRuntime) -> Boolean = { a, b, r ->
    OpEqualsImpl(a, b, true, r)
}

internal val OpEqualsComparator : (Comparable<*>, Comparable<*>,ScriptRuntime) -> Boolean =
    { a, b, r ->
        OpEqualsImpl(a, b, false, r)
    }

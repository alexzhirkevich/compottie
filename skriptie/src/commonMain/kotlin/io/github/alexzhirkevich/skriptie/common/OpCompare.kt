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

internal fun  OpNot(
    condition : Expression,
    isFalse : (Any?) -> Boolean,
) = Expression {
    isFalse(condition(it))
}



internal val OpGreaterComparator : (Comparable<*>, Comparable<*>, ScriptRuntime) -> Boolean = { a, b, r ->

    val ka = r.toKotlin(a)
    val kb = r.toKotlin(b)

    if (ka is Number || kb is Number) {
        r.toNumber(a).toDouble() > r.toNumber(b).toDouble()
    } else {
        a.toString() > b.toString()
    }
}

internal val OpLessComparator : (Comparable<*>, Comparable<*>, ScriptRuntime) -> Boolean = { a, b, r ->
    val ka = r.toKotlin(a)
    val kb = r.toKotlin(b)

    if (ka is Number || kb is Number) {
        r.toNumber(a).toDouble() < r.toNumber(b).toDouble()
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

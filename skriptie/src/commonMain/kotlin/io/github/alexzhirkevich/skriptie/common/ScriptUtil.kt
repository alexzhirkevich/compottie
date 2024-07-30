package io.github.alexzhirkevich.skriptie.common

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal fun <C : ScriptContext> Delegate(
    a : Expression<C>,
    b : Expression<C>,
    op : (Any?, Any?) -> Any?
) = Expression<C> { op(a(it), b(it)) }

internal fun <C : ScriptContext> Delegate(a : Expression<C>, op : (Any?) -> Any?) = Expression<C> {
    op(a(it))
}

internal fun <T : Any?> checkNotEmpty(value : T?) : T {
    check(value != null && value != Unit){
        val type = if (value == null) "null" else "undefined"
        "TypeError: Cannot read properties of $type"
    }
    return value
}

internal fun unresolvedReference(ref : String, obj : String? = null) : Nothing =
    if (obj != null)
        error("Unresolved reference '$ref' for $obj")
    else error("Unresolved reference: $ref")


internal fun <C : ScriptContext, T, R : Any> Expression<C>.cast(block: (T) -> R) : Expression<C> =
    Expression { block(invoke(it) as T) }

internal fun <C : ScriptContext, T, R : Any> Expression<C>.withCast(block: T.(
    context: C,
) -> R) : Expression<C> = Expression {
    block(invoke(it) as T, it)
}

internal operator fun Any.get(index : Int) : Any {
    return checkNotNull(tryGet(index)){
        "Index $index out of bounds of $this length"
    }
}

internal fun Any.tryGet(index : Int) : Any {
    return when (this) {
        is Map<*, *> -> {
            (this as Map<Int, *>)
            if (index in this) {
                get(index)
            } else {
                Unit
            }
        }

        is List<*> -> this.getOrElse(index) { Unit }
        is Array<*> -> this.getOrElse(index) { Unit }
        is CharSequence -> this.getOrNull(index) ?: Unit
        else -> Unit
    }!!
}


@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEachReversed(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices.reversed()) {
        val item = get(index)
        action(item)
    }
}


@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEachIndexed(action: (Int, T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(index, item)
    }
}

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastAll(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (!predicate(it)) return false }
    return true
}

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return true }
    return false
}

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastFirstOrNull(predicate: (T) -> Boolean): T? {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return it }
    return null
}


@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastSumBy(selector: (T) -> Double): Double {
    contract { callsInPlace(selector) }
    var sum = 0.0
    fastForEach { element ->
        sum += selector(element)
    }
    return sum
}

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R> {
    contract { callsInPlace(transform) }
    val target = ArrayList<R>(size)
    fastForEach {
        target += transform(it)
    }
    return target
}

// TODO: should be fastMaxByOrNull to match stdlib
/**
 * Returns the first element yielding the largest value of the given function or `null` if there
 * are no elements.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R : Comparable<R>> List<T>.fastMaxBy(selector: (T) -> R): T? {
    contract { callsInPlace(selector) }
    if (isEmpty()) return null
    var maxElem = get(0)
    var maxValue = selector(maxElem)
    for (i in 1..lastIndex) {
        val e = get(i)
        val v = selector(e)
        if (maxValue < v) {
            maxElem = e
            maxValue = v
        }
    }
    return maxElem
}

/**
 * Returns the last element matching the given [predicate], or `null` if no such element was found.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastLastOrNull(predicate: (T) -> Boolean): T? {
    contract { callsInPlace(predicate) }
    for (index in indices.reversed()) {
        val item = get(index)
        if (predicate(item)) return item
    }
    return null
}

/**
 * Returns a list containing only elements matching the given [predicate].
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastFilter(predicate: (T) -> Boolean): List<T> {
    contract { callsInPlace(predicate) }
    val target = ArrayList<T>(size)
    fastForEach {
        if (predicate(it)) target += (it)
    }
    return target
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
internal inline fun <T, R> List<T>.fastMapIndexed(
    transform: (index: Int, T) -> R
): List<R> {
    contract { callsInPlace(transform) }
    val target = ArrayList<R>(size)
    fastForEachIndexed { index, e ->
        target += transform(index, e)
    }
    return target
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
internal inline fun <T, R> List<T>.fastMapIndexedNotNull(
    transform: (index: Int, T) -> R?
): List<R> {
    contract { callsInPlace(transform) }
    val target = ArrayList<R>(size)
    fastForEachIndexed { index, e ->
        transform(index, e)?.let { target += it }
    }
    return target
}

/**
 * Returns the largest value among all values produced by selector function applied to each element
 * in the collection or null if there are no elements.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R : Comparable<R>> List<T>.fastMaxOfOrNull(selector: (T) -> R): R? {
    contract { callsInPlace(selector) }
    if (isEmpty()) return null
    var maxValue = selector(get(0))
    for (i in 1..lastIndex) {
        val v = selector(get(i))
        if (v > maxValue) maxValue = v
    }
    return maxValue
}

/**
 * Returns a list containing the results of applying the given [transform] function
 * to each element in the original collection.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R> List<T>.fastMapNotNull(transform: (T) -> R?): List<R> {
    contract { callsInPlace(transform) }
    val target = ArrayList<R>(size)
    fastForEach { e ->
        transform(e)?.let { target += it }
    }
    return target
}


/**
 * Returns a single list of all elements yielded from results of [transform] function being invoked
 * on each element of original collection.
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@Suppress("BanInlineOptIn") // Treat Kotlin Contracts as non-experimental.
@OptIn(ExperimentalContracts::class)
internal inline fun <T, R> List<T>.fastFlatMap(transform: (T) -> Iterable<R>): List<R> {
    contract { callsInPlace(transform) }
    val target = ArrayList<R>(size)
    fastForEach { e ->
        val list = transform(e)
        target.addAll(list)
    }
    return target
}

/**
 * Returns the first value that [predicate] returns `true` for
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 *
 * @throws [NoSuchElementException] if no such element is found
 */
@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastFirst(predicate: (T) -> Boolean): T {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return it }
    throw NoSuchElementException("Collection contains no element matching the predicate.")
}

/**
 * Copied from Appendable.kt
 */
private fun <T> Appendable.appendElement(element: T, transform: ((T) -> CharSequence)?) {
    when {
        transform != null -> append(transform(element))
        element is CharSequence? -> append(element)
        element is Char -> append(element)
        else -> append(element.toString())
    }
}

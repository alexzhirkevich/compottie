package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.common.Callable
import io.github.alexzhirkevich.skriptie.common.Function
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.unresolvedReference
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

public interface ESObject : ESAny {

    public val keys : Set<String>
    public val entries : List<List<Any?>>

    public operator fun set(variable: Any?, value: Any?)
    public operator fun contains(variable: Any?): Boolean

    override fun invoke(function: String, context: ScriptRuntime, arguments: List<Expression>): Any? {
        val f = get(function)
        if (f !is Callable) {
            when (f){
                "toString" -> return toString()
                else -> unresolvedReference(function)
            }
        }
        return f.invoke(arguments, context)
    }
}

internal open class ESObjectBase(
    internal val name : String,
    private val map : MutableMap<Any?, Any?> = mutableMapOf()
) : ESObject {

    override val keys: Set<String>
        get() = map.keys.map { it.toString() }.toSet()

    override val entries: List<List<Any?>>
        get() = map.entries.map { listOf(it.key, it.value) }

    override fun get(variable: Any?): Any? {
        return if (variable in map)
            map[variable]
        else Unit
    }
    override fun set(variable: Any?, value: Any?) {
        map[variable] = value
    }

    override fun contains(variable: Any?): Boolean = variable in map

    override fun toString(): String {
        return if (name.isNotBlank()){
            "[object $name]"
        } else {
            "[object]"
        }
    }
}


public sealed interface ObjectScope {

    public infix fun String.eq(value: Any?)

    public fun String.func(
        vararg args: FunctionParam,
        body: ScriptRuntime.(args: List<Any?>) -> Any?
    )

    public fun String.func(
        vararg args: String,
        params: (String) -> FunctionParam = { FunctionParam(it) },
        body: ScriptRuntime.(args: List<Any?>) -> Any?
    ) {
        func(
            args = args.map(params).toTypedArray(),
            body = body
        )
    }
}

private class ObjectScopeImpl(
    name: String,
    val o : ESObject = ESObjectBase(name)
) : ObjectScope {

    override fun String.func(
        vararg args: FunctionParam,
        body: ScriptRuntime.(args: List<Any?>) -> Any?
    ) {
        this eq Function(
            this,
            parameters = args.toList(),
            body = { ctx ->
                with(ctx) {
                    body(args.map { ctx[it.name] })
                }
            }
        )
    }

    override fun String.eq(value: Any?) {
        o.set(this, value)
    }
}


public fun  Object(name: String, builder : ObjectScope.() -> Unit) : ESObject {
    return ObjectScopeImpl(name).also(builder).o
}


internal fun ESObject.init(scope: ObjectScope.() -> Unit) {
    ObjectScopeImpl("", this).apply(scope)
}

internal fun func(
    vararg args: String,
    params: (String) -> FunctionParam = { FunctionParam(it) },
    body: ScriptRuntime.(args: List<Any?>) -> Any?
) : PropertyDelegateProvider<ESObject, ReadOnlyProperty<ESObject, Callable>> =  func(
    args = args.map(params).toTypedArray(),
    body = body
)

internal fun func(
    vararg args: FunctionParam,
    body: ScriptRuntime.(args: List<Any?>) -> Any?
): PropertyDelegateProvider<ESObject, ReadOnlyProperty<ESObject, Callable>> =  PropertyDelegateProvider { obj, prop ->
    obj.set(prop.name, Function(
        name = prop.name,
        parameters = args.toList(),
        body = {
            with(it) {
                body(args.map { get(it.name) })
            }
        }
    ))

    ReadOnlyProperty { thisRef, property ->
        thisRef[property.name] as Callable
    }
}


internal fun  String.func(
    vararg args: FunctionParam,
    body: ScriptRuntime.(args: List<Any?>) -> Any?
) = Function(
    this,
    parameters = args.toList(),
    body = {
        with(it) {
            body(args.map { get(it.name) })
        }
    }
)


internal fun  String.func(
    vararg args: String,
    params: (String) -> FunctionParam = { FunctionParam(it) },
    body: ScriptRuntime.(args: List<Any?>) -> Any?
) : Function = func(
    args = args.map(params).toTypedArray(),
    body = body
)
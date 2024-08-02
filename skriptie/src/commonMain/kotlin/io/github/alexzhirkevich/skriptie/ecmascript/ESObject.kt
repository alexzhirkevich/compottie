package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.common.Callable
import io.github.alexzhirkevich.skriptie.common.Function
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.OpGetVariable
import io.github.alexzhirkevich.skriptie.common.unresolvedReference

public interface ESObject : ESAny {
    public operator fun set(variable: String, value: Any?)
    public operator fun contains(variable: String): Boolean

    override fun invoke(function: String, context: ScriptRuntime, arguments: List<Expression>): Any? {
        val f = get(function)
        if (f !is Callable) {
            unresolvedReference(function)
        }
        return f.invoke(arguments, context)
    }
}

internal open class ESObjectBase(
    internal val name : String,
    private val map : MutableMap<String, Any?> = mutableMapOf()
) : ESObject {

    init {
        if ("toString" !in map){
            map["toString"] = Function(
                name = "toString",
                parameters = emptyList(),
                body = Expression { toString() })
        }
    }

    override fun get(variable: String): Any? {
        return if (contains(variable)) map[variable] else Unit
    }
    override fun set(variable: String, value: Any?) {
        map[variable] = value
    }

    override fun contains(variable: String): Boolean = variable in map

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
        body: (args: List<Expression>) -> Expression
    )

    public fun String.func(
        vararg args: String,
        params: (String) -> FunctionParam = { FunctionParam(it) },
        body: (args: List<Expression>) -> Expression
    ) {
        func(
            args = args.map(params).toTypedArray(),
            body = body
        )
    }
}

private class ObjectScopeImpl(name: String) : ObjectScope {
    val o = ESObjectBase(name)

    override fun String.func(
        vararg args: FunctionParam,
        body: (args: List<Expression>) -> Expression
    ) {
        this eq Function(
            this,
            parameters = args.toList(),
            body = body(args.map { OpGetVariable(it.name, null) })
        )
    }

    override fun String.eq(value: Any?) {
        o[this] = value
    }
}


public fun  Object(name: String, builder : ObjectScope.() -> Unit) : ESObject {
    return ObjectScopeImpl(name).also(builder).o
}

internal fun ESObject.setFunction(function: Function) = set(function.name, function)

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
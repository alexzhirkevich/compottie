package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.OpFunction
import io.github.alexzhirkevich.skriptie.common.OpGetVariable
import io.github.alexzhirkevich.skriptie.common.unresolvedReference

public interface ESObject<C : ScriptRuntime> : ESAny<C> {
    public operator fun set(property: String, value: Any?)
    public operator fun contains(property: String): Boolean

    override fun invoke(function: String, context: C, arguments: List<Expression<C>>): Any? {
        val f = get(function)
        if (f !is OpFunction<*>) {
            unresolvedReference(function)
        }

        f as OpFunction<C>
        return f.invoke(arguments, context)
    }
}

public class ObjectImpl<C : ScriptRuntime>(
    private val name : String,
    private val map : MutableMap<String, Any?> = mutableMapOf()
) : ESObject<C> {

    init {
        if ("toString" !in map){
            map["toString"] = OpFunction(
                name = "toString",
                parameters = emptyList(),
                body = Expression { toString() })
        }
    }

    override fun get(property: String): Any? {
        return if (contains(property)) map[property] else Unit
    }
    override fun set(property: String, value: Any?) {
        map[property] = value
    }

    override fun contains(property: String): Boolean = property in map

    override fun toString(): String {
        return if (name.isNotBlank()){
            "[object $name]"
        } else {
            "[object]"
        }
    }
}


public sealed interface ObjectScope<C : ScriptRuntime> {

    public infix fun String.eq(value: Any?)

    public fun String.func(
        vararg args: FunctionParam<C>,
        body: (args: List<Expression<C>>) -> Expression<C>
    )

    public fun String.func(
        vararg args: String,
        params: (String) -> FunctionParam<C> = { FunctionParam(it) },
        body: (args: List<Expression<C>>) -> Expression<C>
    ) {
        func(
            args = args.map(params).toTypedArray(),
            body = body
        )
    }
}

private class ObjectScopeImpl<C : ScriptRuntime>(name: String) : ObjectScope<C> {
    val o = ObjectImpl<C>(name)

    override fun String.func(
        vararg args: FunctionParam<C>,
        body: (args: List<Expression<C>>) -> Expression<C>
    ) {
        this eq OpFunction(
            this,
            parameters = args.toList(),
            body = body(args.map { OpGetVariable(it.name, null) })
        )
    }

    override fun String.eq(value: Any?) {
        o[this] = value
    }
}

public fun <C: ScriptRuntime> Object(name: String, builder : ObjectScope<C>.() -> Unit) : ESObject<C> {
    return ObjectScopeImpl<C>(name).also(builder).o
}
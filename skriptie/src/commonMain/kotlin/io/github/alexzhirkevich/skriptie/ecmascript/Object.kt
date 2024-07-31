package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptContext
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.common.OpFunction
import io.github.alexzhirkevich.skriptie.common.OpGetVariable

internal class Object(
    private val name : String,
    private val map : MutableMap<String, Any?> = mutableMapOf()
) : MutableMap<String,Any?> by map {
    override fun toString(): String {
        return if (name.isNotBlank()){
            "[object $name]"
        } else {
            "[object]"
        }
    }
}


internal sealed interface ObjectScope<C : ScriptContext> {

    infix fun String.eq(value: Any?)

    fun String.func(
        vararg args: FunctionParam<C>,
        body: (args: List<Expression<C>>) -> Expression<C>
    )

    fun String.func(
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

private class ObjectScopeImpl<C : ScriptContext>(name: String) : ObjectScope<C> {
    val o = Object(name)

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

internal fun <C: ScriptContext> obj(name: String, builder : ObjectScope<C>.() -> Unit) : Object {
    return ObjectScopeImpl<C>(name).also(builder).o
}
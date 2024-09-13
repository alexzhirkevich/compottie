package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.common.Callable
import io.github.alexzhirkevich.skriptie.common.Function
import io.github.alexzhirkevich.skriptie.common.Named
import io.github.alexzhirkevich.skriptie.common.OpConstant
import io.github.alexzhirkevich.skriptie.common.fastForEach
import io.github.alexzhirkevich.skriptie.invoke

internal interface ESClass : ESObject, Callable, Named {

    val functions : List<Function>

    val construct: Function?

    val extends : Expression?

    val constructorClass : Expression?

    val isInitialized : Boolean get() = true

    val static : List<StaticClassMember> get() = emptyList()

    override val type: String
        get() = "object"

    fun newInstance(args: List<Expression>, context: ScriptRuntime) : Any? =
        invoke(args, context)
}

internal fun ESClass.superFunctions(runtime: ScriptRuntime) : List<Function> {
    val extendsClass = extends?.invoke(runtime) as? ESClass
        ?: return emptyList()

    return (extendsClass.superFunctions(runtime) + extendsClass.functions).associateBy { it.name }.values.toList()
}

internal tailrec fun ESClass.instanceOf(
    any: Any?,
    runtime: ScriptRuntime,
    extends: Expression? = this.extends
) : Boolean {

    if (constructorClass?.invoke(runtime) == any || any is ESObjectAccessor) {
        return true
    }

    val e = extends?.invoke(runtime)?.let { it as? ESClass? } ?: return false

    if (e == any) {
        return true
    }

    return instanceOf(any, runtime, e.extends)
}

internal sealed interface StaticClassMember {

    fun assignTo(clazz : ESClass, runtime: ScriptRuntime)

    class Variable(val name : String, val init : Expression) : StaticClassMember {
        override fun assignTo(clazz: ESClass, runtime: ScriptRuntime) {
            clazz[name] = init(runtime)
        }
    }

    class Method(val function: Function) : StaticClassMember {
        override fun assignTo(clazz: ESClass, runtime: ScriptRuntime) {
            clazz[function.name] = function
        }
    }
}

internal open class ESClassBase(
    override val name : String,
    final override val functions : List<Function>,
    final override val construct: Function?,
    final override val extends: Expression?,
    final override val static: List<StaticClassMember>
) : ESObjectBase(name), ESClass {

    private var isSuperInitialized = false

    override val isInitialized: Boolean get() =
        extends == null || isSuperInitialized

    final override var constructorClass: Expression = OpConstant(this)

    override fun invoke(args: List<Expression>, context: ScriptRuntime): Any? {

        val superConstructor = (extends?.invoke(context) as? ESClass)?.construct?.let {
            it.copy(
                body = { r ->
                    if (isSuperInitialized) {
                        throw ReferenceError("Super constructor may only be called once")
                    } else {
                        it.body(r).also { isSuperInitialized = true }
                    }
                }
            )
        }
        if (superConstructor == null && extends != null) {
            isSuperInitialized = true
        }

        val clazz = ESClassBase(
            name = name,
            functions = (superFunctions(context) + functions).map(Function::copy),
            construct = construct?.copy(
                extraVariables = if (superConstructor != null){
                    mapOf("super" to (VariableType.Const to superConstructor))
                } else emptyMap()
            ),
            extends = extends,
            static = static,
        )
        superConstructor?.thisRef = clazz

        clazz.constructorClass = constructorClass
        clazz.functions.fastForEach {
            clazz[it.name] = it.apply { thisRef=clazz }
        }
        clazz.construct?.thisRef = clazz

        clazz.construct?.invoke(args, context)

        return clazz
    }
    override val type: String
        get() = "object"

    override fun toString(): String {
        val properties = keys.joinToString(
            prefix = " ",
            postfix = " ",
            separator = ", "
        ) {
            "$it: ${get(it)}"
        }
        return "$name {$properties}"
    }
}

internal fun ESClassInstantiation(
    name: String,
    args : List<Expression>
) = Expression {
    val clazz = it[name]
    syntaxCheck(clazz is ESClass) {
        "$name is not a constructor"
    }
    clazz.newInstance(args, it)
}
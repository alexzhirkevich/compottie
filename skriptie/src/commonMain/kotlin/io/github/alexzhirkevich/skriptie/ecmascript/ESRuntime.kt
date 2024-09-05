package io.github.alexzhirkevich.skriptie.ecmascript

import io.github.alexzhirkevich.skriptie.DefaultRuntime
import io.github.alexzhirkevich.skriptie.DefaultScriptIO
import io.github.alexzhirkevich.skriptie.Expression
import io.github.alexzhirkevich.skriptie.ScriptIO
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.VariableType
import io.github.alexzhirkevich.skriptie.common.TypeError
import kotlin.jvm.JvmInline

public abstract class ESRuntime(
    override val io: ScriptIO = DefaultScriptIO,
) : DefaultRuntime(), ESObject {

    override val comparator: Comparator<Any?> by lazy {
        ESComparator(this)
    }

    override val keys: Set<String> get() = emptySet()
    override val entries: List<List<Any?>> get() = emptyList()

    init {
        init()
    }

    override fun reset() {
        super.reset()
        init()
    }

    private fun init() {
        set("Object", ESObjectAccessor(), VariableType.Const)
        set("Number", ESNumber(), VariableType.Const)
        set("globalThis", this, VariableType.Const)
        set("Infinity", Double.POSITIVE_INFINITY, VariableType.Const)
        set("NaN", Double.NaN, VariableType.Const)
        set("undefined", Unit, VariableType.Const)
    }

    final override fun get(variable: Any?): Any? {
        if (variable in this){
            return super.get(variable)
        }

        val globalThis = get("globalThis") as? ESObject? ?: return super.get(variable)

        if (variable in globalThis){
            return globalThis[variable]
        }

        return super.get(variable)
    }

    final override fun set(variable: Any?, value: Any?) {
        set(variable, value, VariableType.Local)
    }

    override fun invoke(
        function: String,
        context: ScriptRuntime,
        arguments: List<Expression>
    ): Any? {
        throw TypeError("ScriptRuntime is not a function")
    }
}

@JvmInline
internal value class ESComparator(
    private val runtime: ScriptRuntime
) : Comparator<Any?> {

    override fun compare(a: Any?, b: Any?): Int {
        val ra = runtime.toKotlin(a)
        val rb = runtime.toKotlin(b)
        return if (ra is Number && rb is Number) {
            ra.toDouble().compareTo(rb.toDouble())
        } else {
            ra.toString().compareTo(rb.toString())
        }
    }
}
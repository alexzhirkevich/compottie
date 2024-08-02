package js

import io.github.alexzhirkevich.skriptie.ScriptEngine
import io.github.alexzhirkevich.skriptie.ecmascript.EcmascriptInterpreter
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JSGlobalContext
import io.github.alexzhirkevich.skriptie.javascript.JSRuntime
import kotlin.test.assertEquals

internal fun Any?.assertEqualsTo(other : Any?) = assertEquals(other,this)
internal fun Any?.assertEqualsTo(other : Double, tolerance: Double = 0.0001) {
    assertEquals(other, this as Double, tolerance)
}

internal fun String.eval() : Any? {
    val runtime = JSRuntime()

    return ScriptEngine(
        runtime,
        EcmascriptInterpreter(
            JSGlobalContext(false),
            runtime
        )
    ).invoke(this)
}

package js

import io.github.alexzhirkevich.skriptie.ScriptEngine
import io.github.alexzhirkevich.skriptie.ecmascript.EcmascriptInterpreter
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JSInterpretationContext
import io.github.alexzhirkevich.skriptie.javascript.JSScriptContext
import kotlin.test.assertEquals

internal fun Any?.assertEqualsTo(other : Any?) = assertEquals(other,this)
internal fun Any?.assertEqualsTo(other : Double, tolerance: Double = 0.0001) {
    assertEquals(other, this as Double, tolerance)
}

internal fun String.eval() : Any? {
    return ScriptEngine(
        JSScriptContext(),
        EcmascriptInterpreter(JSInterpretationContext())
    ).invoke(this)
}

package js

import io.github.alexzhirkevich.skriptie.DefaultScriptIO
import io.github.alexzhirkevich.skriptie.ScriptEngine
import io.github.alexzhirkevich.skriptie.ScriptIO
import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.ecmascript.ESInterpreter
import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JSLangContext
import io.github.alexzhirkevich.skriptie.javascript.JSRuntime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal fun Any?.assertEqualsTo(other : Any?) = assertEquals(other,this)
internal fun Any?.assertEqualsTo(other : Double, tolerance: Double = 0.0001) {
    assertTrue("$this is not a Double") { this is Double }
    assertEquals(other, this as Double, tolerance)
}

internal fun String.eval(runtime: ScriptRuntime = JSRuntime()) : Any? {
    return ScriptEngine(runtime, ESInterpreter(JSLangContext)).invoke(this)
}

internal fun String.eval(io : ScriptIO = DefaultScriptIO, runtime: ScriptRuntime = JSRuntime(io)) : Any? {
    return ScriptEngine(runtime, ESInterpreter(JSLangContext)).invoke(this)
}

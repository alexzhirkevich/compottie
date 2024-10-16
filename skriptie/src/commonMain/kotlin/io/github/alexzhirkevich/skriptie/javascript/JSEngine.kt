package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.ScriptEngine
import io.github.alexzhirkevich.skriptie.ecmascript.ESInterpreter
import io.github.alexzhirkevich.skriptie.evaluate

public fun JSEngine(
    runtime: JSRuntime = JSRuntime(),
    interpreter: ESInterpreter = ESInterpreter(JSLangContext)
) : ScriptEngine = ScriptEngine(runtime, interpreter)

/**
 * Simplest way to evaluate some JavaScript code.
 *
 * Unlike Kotlin/JS, [script] is not required to be a compile-time constant.
 * To get a persistent runtime or compile a reusable script use [JSEngine].
 * */
public fun js(script : String) : Any? = JSEngine(interpreter = JSInterpreter).evaluate(script)

private val JSInterpreter = ESInterpreter(JSLangContext)

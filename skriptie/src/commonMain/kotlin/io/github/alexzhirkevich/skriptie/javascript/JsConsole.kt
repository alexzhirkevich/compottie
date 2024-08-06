package io.github.alexzhirkevich.skriptie.javascript

import io.github.alexzhirkevich.skriptie.ScriptRuntime
import io.github.alexzhirkevich.skriptie.common.FunctionParam
import io.github.alexzhirkevich.skriptie.ecmascript.Object

internal fun JsConsole() = Object("console") {
    "log".func(FunctionParam("data", isVararg = true)) { out(it, io::out) }
    "err".func(FunctionParam("data", isVararg = true)) { out(it, io::err) }
}

private fun ScriptRuntime.out(message : List<Any?>, out : (Any?) -> Unit){
    if (message.isEmpty() || (message[0] as List<*>).isEmpty()) {
        return
    }
    val args = message[0] as List<*>
    if (args.size == 1) {
        out(toKotlin(args[0]))
    } else {
        out(args.map(::toKotlin))
    }
}

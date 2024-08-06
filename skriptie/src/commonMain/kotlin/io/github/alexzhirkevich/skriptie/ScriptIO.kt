package io.github.alexzhirkevich.skriptie

public interface ScriptIO {

    public fun out(message: Any?)

    public fun err(message: Any?)
}

public object DefaultScriptIO : ScriptIO {
    override fun out(message: Any?) {
        println(message)
    }

    override fun err(message: Any?) {
        if (message is Throwable){
            message.printStackTrace()
        } else {
            println(message)
        }
    }
}
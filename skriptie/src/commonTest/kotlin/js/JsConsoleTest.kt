package js

import io.github.alexzhirkevich.skriptie.ScriptIO
import kotlin.test.Test

class JsConsoleTest {

    @Test
    fun out(){

        var out : Any? = null
        var err : Any? = null

        val io = object : ScriptIO {
            override fun out(message: Any?) { out = message }
            override fun err(message: Any?) { err = message }
        }

        "console.log(123)".eval(io).assertEqualsTo(Unit)

        out.assertEqualsTo(123L)
        err.assertEqualsTo(null)
        out = null

        "console.log('123')".eval(io).assertEqualsTo(Unit)

        out.assertEqualsTo("123")
        err.assertEqualsTo(null)
        out = null

        "console.err('123')".eval(io).assertEqualsTo(Unit)

        err.assertEqualsTo("123")
        out.assertEqualsTo(null)
    }

    @Test
    fun empty(){
        var out : Any? = null
        var err : Any? = null

        val io = object : ScriptIO {
            override fun out(message: Any?) { out = message }
            override fun err(message: Any?) { err = message }
        }

        "console.log()".eval(io).assertEqualsTo(Unit)
        out.assertEqualsTo(null)
        err.assertEqualsTo(null)
    }

    @Test
    fun multiple(){
        var out : Any? = null
        var err : Any? = null

        val io = object : ScriptIO {
            override fun out(message: Any?) { out = message }
            override fun err(message: Any?) { err = message }
        }

        "console.log(123, 456, '789')".eval(io).assertEqualsTo(Unit)

        out.assertEqualsTo(listOf(123L, 456L, "789"))
        err.assertEqualsTo(null)
        out = null

        "console.err(123, 456, '789')".eval(io).assertEqualsTo(Unit)

        err.assertEqualsTo(listOf(123L, 456L, "789"))
        out.assertEqualsTo(null)
    }
}
package js

import io.github.alexzhirkevich.skriptie.ecmascript.ESObject
import io.github.alexzhirkevich.skriptie.javascript.JSRuntime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObjectTest {

    @Test
    fun context(){

        val obj = "{ name : 'test'}".eval() as ESObject

        obj["name"].toString().assertEqualsTo("test")


        "typeof {}".eval().assertEqualsTo("object")
        "let x = {}; typeof x".eval().assertEqualsTo("object")
        "let x = ({}); typeof x".eval().assertEqualsTo("object")
        "let x = Object({}); typeof x".eval().assertEqualsTo("object")
        "let x = 1; if ({}) { x = 2 }; x".eval().assertEqualsTo(2L)
        """
            function test(x) { 
                return x
            }
            typeof test({})
        """.trimIndent().eval().assertEqualsTo("object")
    }

    @Test
    fun syntax(){

        val runtime = JSRuntime()

        """
            let obj = {
                string : "string",
                number : 123,
                f : function() { },
                af : () => {}
            } 
        """.trimIndent().eval(runtime)

        "typeof(obj.string)".eval(runtime).assertEqualsTo("string")
        "typeof(obj.number)".eval(runtime).assertEqualsTo("number")
        "typeof(obj.f)".eval(runtime).assertEqualsTo("function")
        "typeof(obj.af)".eval(runtime).assertEqualsTo("function")
        "typeof(obj.nothing)".eval(runtime).assertEqualsTo("undefined")
    }

    @Test
    fun getters(){
        "let obj = { name : 'string' }; obj['name']".eval().assertEqualsTo("string")
        "let obj = { name : 'string' }; obj.name".eval().assertEqualsTo("string")
    }

    @Test
    fun setters(){
        "let obj = {}; obj['name'] = 213; obj.name".eval().assertEqualsTo(213L)
        "let obj = {}; obj.name = 213; obj.name".eval().assertEqualsTo(213L)
    }

    @Test
    fun global_object(){
        "typeof Object".eval().assertEqualsTo("function")

        "Object.keys({ name : 'test' })".eval().assertEqualsTo(listOf("name"))
        "Object.keys({ name : 'test', x : 1 })".eval().assertEqualsTo(listOf("name","x"))
        ("Object.keys(1)".eval() as List<*>).size.assertEqualsTo(0)

        "Object.entries({ name : 'test' })".eval()
            .assertEqualsTo(listOf(listOf("name", "test")))
        "Object.entries({ name : 'test', x : 1 })".eval()
            .assertEqualsTo(listOf(listOf("name", "test"), listOf("x", 1L)))
        ("Object.entries(1)".eval() as List<*>).size.assertEqualsTo(0)

    }

    @Test
    fun contains(){
        val runtime = JSRuntime()
        "let obj = { name : 'test'}".eval(runtime)
        assertTrue { "'name' in obj".eval(runtime) as Boolean }
        assertFalse { "'something' in obj".eval(runtime) as Boolean }
    }
}
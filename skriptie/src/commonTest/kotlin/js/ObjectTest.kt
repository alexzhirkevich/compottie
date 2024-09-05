package js

import io.github.alexzhirkevich.skriptie.ecmascript.ESObject
import kotlin.test.Test

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
        """
            let obj = {
                string : "string",
                number : 123,
                f : function() { },
                af : () => {}
            } 
            
            typeof(obj.string) + ' ' + typeof(obj.number) + ' ' + typeof(obj.f) + ' ' + typeof(obj.af) + ' ' + typeof obj.nothing
        """.trimIndent().eval().assertEqualsTo("string number function function undefined")
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
}
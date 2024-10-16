package js

import io.github.alexzhirkevich.skriptie.ecmascript.ReferenceError
import io.github.alexzhirkevich.skriptie.ecmascript.SyntaxError
import io.github.alexzhirkevich.skriptie.ecmascript.TypeError
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SyntaxTest {

    @Test
    fun newline_property() {
        """
            Math
                .imul(3,4)
                .toString()
        """.trimIndent().eval().assertEqualsTo("12")

        """
            Math

                .imul(3,4)

                .toString()
        """.trimIndent().eval().assertEqualsTo("12")

        """
            Math

                .imul(3,4)

                .toString()
                ;
        """.trimIndent().eval().assertEqualsTo("12")


        assertFailsWith<SyntaxError> {
            """
            Math
                .imul(3,4);
                .toString()
        """.trimIndent().eval()
        }
    }

    @Test
    fun increment_decrement() {
        "let x = 5; x++".eval().assertEqualsTo(5L)
        "let x = 5; ++x".eval().assertEqualsTo(6L)

        "let x = 5; x--".eval().assertEqualsTo(5L)
        "let x = 5; --x".eval().assertEqualsTo(4L)

        "let x = 0; let y = x++; y".eval().assertEqualsTo(0L)
        "let x = 0; let y = x++; x".eval().assertEqualsTo(1L)
        "let x = 0; let y = ++x; y".eval().assertEqualsTo(1L)
        "let x = 0; let y = ++x; x".eval().assertEqualsTo(1L)

        "let x = 1; let y = x--; y".eval().assertEqualsTo(1L)
        "let x = 1; let y = x--; x".eval().assertEqualsTo(0L)
        "let x = 1; let y = --x; y".eval().assertEqualsTo(0L)
        "let x = 1; let y = --x; x".eval().assertEqualsTo(0L)
    }

    @Test
    fun variable_scopes() {
        """
            var x;
            if(true) {
                x = 5
            }
            x
        """.trimIndent().eval().assertEqualsTo(5L)

        """
            var x = 1;
            if(true){
                let x = 5
            }
            x
        """.trimIndent().eval().assertEqualsTo(1L)


        """
            if(true){
                var x = 5
            }
            x
        """.trimIndent().eval().assertEqualsTo(5L)

        assertFailsWith<ReferenceError> {
            """
                if(true){
                    let x = 5
                }
                x
            """.trimIndent().eval()
        }
        assertFailsWith<ReferenceError> {
            """
                if(true){
                    const x = 5
                }
                x
            """.trimIndent().eval()
        }
    }

    @Test
    fun constMutating() {
        assertFailsWith<TypeError> { "const x = 1; x++".eval() }
    }

    @Test
    fun variable_redeclaration() {
        assertFailsWith<SyntaxError> { "const x = 1; const x = 2".eval() }
        assertFailsWith<SyntaxError> { "const x = 1; let x = 2".eval() }
        assertFailsWith<SyntaxError> { "const x = 1; var x = 2".eval() }
        assertFailsWith<SyntaxError> { "var x = 1; const x = 2".eval() }
        assertFailsWith<SyntaxError> { "var x = 1; let x = 2".eval() }
        assertFailsWith<SyntaxError> { "var x = 1; var x = 2".eval() }
        assertFailsWith<SyntaxError> { "let x = 1; const x = 2".eval() }
        assertFailsWith<SyntaxError> { "let x = 1; var x = 2".eval() }
        assertFailsWith<SyntaxError> { "let x = 1; let x = 2".eval() }

        "const x = 1; { const x = 2 }; x".eval().assertEqualsTo(1L)
        "const x = 1; { let x = 2 }; x".eval().assertEqualsTo(1L)
        assertFailsWith<SyntaxError> {
            "const x = 1; { var x = 2 }; x".eval().assertEqualsTo(1L)
        }
        "var x = 1; { const x = 2 }; x".eval().assertEqualsTo(1L)
        "var x = 1; { let x = 2 }; x".eval().assertEqualsTo(1L)
        assertFailsWith<SyntaxError> {
            "var x = 1; { var x = 2 }; x".eval().assertEqualsTo(1L)
        }
        "let x = 1; { const x = 2 }; x".eval().assertEqualsTo(1L)
        assertFailsWith<SyntaxError> {
            "let x = 1; { var x = 2 }; x".eval().assertEqualsTo(1L)
        }
        "let x = 1; { let x = 2 }; x".eval().assertEqualsTo(1L)
    }

    @Test
    fun typeOf() {
        "typeof(1)".eval().assertEqualsTo("number")
        "typeof(null)".eval().assertEqualsTo("object")
        "typeof(undefined)".eval().assertEqualsTo("undefined")
        "typeof('str')".eval().assertEqualsTo("string")
        "typeof('str') + 123".eval().assertEqualsTo("string123")

        "typeof 1".eval().assertEqualsTo("number")
        "typeof null".eval().assertEqualsTo("object")
        "typeof undefined".eval().assertEqualsTo("undefined")

        "typeof 1===1".eval().assertEqualsTo("boolean")

        "let x = 1; typeof x++".eval().assertEqualsTo("number")
        assertFailsWith<SyntaxError> {
            "let x = 1; typeof x = 2".eval()
        }
        assertFailsWith<SyntaxError> {
            "let x = 1; typeof x += 2".eval()
        }
    }

    @Test
    fun tryCatch() {
        """
            let error = undefined
            try {
                let x = null
                x.test = 1
            } catch(x) {
                error = x.message
            }
            error
        """.trimIndent().eval().assertEqualsTo("Cannot set properties of null (setting 'test')")

        """
            let error = undefined
            try {
                throw 'test'
            } catch(x) {
                error = x
            }
            error
        """.trimIndent().eval().assertEqualsTo("test")

        """
            let a = 1
            try {
                throw 'test'
            } catch(x) {
                a++
            } finally {
                a++
            }
            a
        """.trimIndent().eval().assertEqualsTo(3L)

        """
            let a = 1
            try {
               
            } catch(x) {
                a++
            } finally {
                a++
            }
            a
        """.trimIndent().eval().assertEqualsTo(2L)
    }

    @Test
    fun operator_precedence_and_associativity() {
        "1 + 2 ** 3 * 4 / 5 >> 6".eval().assertEqualsTo(0L)

        "2 ** 3 / 3 ** 2".eval().assertEqualsTo(0.8888888888888888)
     // (2 ** 3) / (3 ** 2)

        "4 / 3 / 2".eval().assertEqualsTo(0.6666)
    }

}
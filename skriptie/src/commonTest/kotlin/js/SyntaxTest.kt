package js

import io.github.alexzhirkevich.skriptie.common.ReferenceError
import io.github.alexzhirkevich.skriptie.common.SyntaxError
import io.github.alexzhirkevich.skriptie.common.TypeError
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
}
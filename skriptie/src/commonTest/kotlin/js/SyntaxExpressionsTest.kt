package js

import kotlin.test.Test
import kotlin.test.assertFails

class SyntaxExpressionsTest {

    @Test
    fun newline_property() {
        """
            Math
                .imul(3,4)
                .toString()
        """.trimIndent().runJs().assertEqualsTo("12")

        """
            Math

                .imul(3,4)

                .toString()
        """.trimIndent().runJs().assertEqualsTo("12")

        """
            Math

                .imul(3,4)

                .toString()
                ;
        """.trimIndent().runJs().assertEqualsTo("12")


        assertFails {
            """
            Math
                .imul(3,4);
                .toString()
        """.trimIndent().runJs()
        }
    }

    @Test
    fun increment_decrement() {
        "let x = 5; x++".runJs().assertEqualsTo(6L)
        "let x = 5; ++x".runJs().assertEqualsTo(6L)

        "let x = 5; x--".runJs().assertEqualsTo(4L)
        "let x = 5; --x".runJs().assertEqualsTo(4L)

        "let x = 5;  --x".runJs().assertEqualsTo(4L)
        "let x = 5;  ++x".runJs().assertEqualsTo(6L)
        "let x = 5;  x--".runJs().assertEqualsTo(4L)
        "let x = 5;  x++".runJs().assertEqualsTo(6L)
    }

    @Test
    fun variable_scopes() {
        """
            var x;
            if(true) {
                x = 5
            }
            x
        """.trimIndent().runJs().assertEqualsTo(5L)

        """
            var x = 1;
            if(true){
                let x = 5
            }
            x
        """.trimIndent().runJs().assertEqualsTo(1L)
    }

    @Test
    fun constMutating() {
        assertFails {
            """
                const x = 1;
                x++
            """.trimIndent().runJs()
        }
    }
}
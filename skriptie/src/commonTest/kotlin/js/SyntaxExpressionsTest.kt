package js

import expressions.ret
import kotlin.test.Test
import kotlin.test.assertFails

class SyntaxExpressionsTest {

    @Test
    fun newline_property() {
        """
            Math
                .imul(3,4)
                .toString()
        """.trimIndent().assertSimpleExprEquals("12")

        """
            Math
            
                .imul(3,4)
                
                .toString()
        """.trimIndent().assertSimpleExprEquals("12")

        """
            Math
            
                .imul(3,4)
                
                .toString()
                ;
        """.trimIndent().assertSimpleExprEquals("12")


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
        "let $ret = 5; $ret++".assertSimpleExprReturns(6L)
        "let $ret = 5; ++$ret".assertSimpleExprReturns(6L)

        "let $ret = 5; $ret--".assertSimpleExprReturns(4L)
        "let $ret = 5; --$ret".assertSimpleExprReturns(4L)

        "let x = 5; let $ret = --x".assertSimpleExprReturns(4L)
        "let x = 5; let $ret = ++x".assertSimpleExprReturns(6L)
        "let x = 5; let $ret = x--".assertSimpleExprReturns(4L)
        "let x = 5; let $ret = x++".assertSimpleExprReturns(6L)
    }

    @Test
    fun variable_scopes() {
        """
            var $ret;
            if(true){
                var x = 5
            }
            $ret = x
        """.trimIndent().assertSimpleExprReturns(5L)

        assertFails {
            """
            var $ret = 1; 
            if(true){
                let x = 5
            }
            $ret = x
        """.trimIndent().runJs()
        }
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

    @Test
    fun tryCatch() {
        assertFails {
            "let x = [123".runJs()
        }
    }
}
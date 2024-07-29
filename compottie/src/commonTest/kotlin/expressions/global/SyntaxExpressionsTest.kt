package expressions.global

import expressions.assertExprReturns
import expressions.assertSimpleExprEquals
import expressions.assertSimpleExprReturns
import expressions.executeExpression
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
        """.trimIndent().executeExpression()
        }
    }

    @Test
    fun increment_decrement() {
        "let $ret = 5; $ret++".assertSimpleExprReturns(6)
        "let $ret = 5; ++$ret".assertSimpleExprReturns(6)

        "let $ret = 5; $ret--".assertSimpleExprReturns(4)
        "let $ret = 5; --$ret".assertSimpleExprReturns(4)

        "let x = 5; let $ret = --x".assertSimpleExprReturns(4)
        "let x = 5; let $ret = ++x".assertSimpleExprReturns(6)
        "let x = 5; let $ret = x--".assertSimpleExprReturns(4)
        "let x = 5; let $ret = x++".assertSimpleExprReturns(6)
    }

    @Test
    fun variable_scopes() {
        """
            var $ret;
            if(true){
                var x = 5
            }
            $ret = x
        """.trimIndent().assertSimpleExprReturns(5)

        assertFails {
            """
            var $ret = 1; 
            if(true){
                let x = 5
            }
            $ret = x
        """.trimIndent().executeExpression()
        }
    }

    @Test
    fun const() {
        assertFails {
            """
                const x = 1; 
                x++
            """.trimIndent().executeExpression()
        }
    }
}
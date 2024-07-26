package expressions.global

import expressions.assertSimpleExprEquals
import expressions.executeExpression
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
}
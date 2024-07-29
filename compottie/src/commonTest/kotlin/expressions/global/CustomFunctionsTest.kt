package expressions.global

import expressions.assertExprReturns
import expressions.ret
import kotlin.test.Test

class CustomFunctionsTest {

    @Test
    fun creation() {
        """
            var $ret;
            function test(a,b) { return sum(a,b) }
            $ret = test(1,2)
        """.trimIndent().assertExprReturns(3f)

        """
            var $ret;
            function test(a,b) {
                return sum(a,b)
            }
            $ret = test(1,2)
        """.trimIndent().assertExprReturns(3f)

        """
            var $ret; 
            function test(a,b) 
            {
                let x = b + 1
                return sum(a,x) 
            }
            $ret = test(1,2)
        """.trimIndent().assertExprReturns(4f)
    }


    @Test
    fun defaultArgs(){
        """
            var $ret; 
            function test(a, b = 2) 
            {
                return sum(a,b) 
            }
            $ret = test(1)
        """.trimIndent().assertExprReturns(3f)

        """
            var $ret; 
            function test(a, b = 2) 
            {
                return sum(a,b) 
            }
            $ret = test(2,3)
        """.trimIndent().assertExprReturns(5f)

        """
            var $ret; 
            function test(a = 1, b = 2) 
            {
                return sum(a,b) 
            }
            $ret = test()
        """.trimIndent().assertExprReturns(3f)
    }

    @Test
    fun namedArgs(){
        """
            var $ret; 
            function test(a, b) 
            {
                return sum(a,b) 
            }
            $ret = test(b = 2, a = 1)
        """.trimIndent().assertExprReturns(3f)
    }
}
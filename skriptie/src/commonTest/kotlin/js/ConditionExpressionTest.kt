package js

import expressions.assertExprReturns
import expressions.ret
import kotlin.test.Test

class ConditionExpressionTest {

    @Test
    fun if_with_else() {
        "var $ret = 1; if (true) { $ret = $ret+1 }".assertExprReturns(2f)
        "var $ret = 1; if (true) $ret+=1".assertExprReturns(2f)
        "var $ret = 1; if (1==1) { $ret = $ret+1 }".assertExprReturns(2f)
        "var $ret = 1; if (1==1) $ret +=1".assertExprReturns(2f)
        "var $ret = 1; if (true) { $ret = $ret+1;$ret = $ret+1 }".assertExprReturns(3f)
        "var $ret = 1; if (true) { $ret = $ret+1\n$ret +=1 }".assertExprReturns(3f)

        "var $ret = 1; if (false) { $ret = $ret } else { $ret = $ret+1 }"
            .assertExprReturns(2f)

        "var $ret = 1.0; if (false) $ret +=1"
            .assertExprReturns(1f)

        "var $ret = 1; if (1 != 1) $ret = 0 else { $ret = $ret+1;$ret = $ret+1 }"
            .assertExprReturns(3f)

        "var $ret = 1; if (!(1 == 1)) $ret = 0 else { $ret +=1;$ret = $ret+1 }"
            .assertExprReturns(3f)

        "var $ret = 0; if (true) { if (true) { $ret +=1 } }"
            .assertExprReturns(1f)

        "var $ret = 0; if (true) { if (false) { $ret = 0 } else { $ret += 1 } }"
            .assertExprReturns(1f)

        "var $ret = 0; if (true) if (false) $ret = 0 else $ret += 1 "
            .assertExprReturns(1f)
    }
}
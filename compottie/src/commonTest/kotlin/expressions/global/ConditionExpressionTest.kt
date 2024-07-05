package expressions.global

import expressions.ret
import expressions.assertExpressionReturns
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test

internal class ConditionExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun if_with_else() {
        floatProp.assertExpressionReturns("var $ret = 1; if (true) { $ret = $ret+1 }", 2f)
        floatProp.assertExpressionReturns("var $ret = 1; if (true) $ret+=1", 2f)

        floatProp.assertExpressionReturns("var $ret = 1; if (1==1) { $ret = $ret+1 }", 2f)
        floatProp.assertExpressionReturns("var $ret = 1; if (1==1) $ret +=1", 2f)

        floatProp.assertExpressionReturns("var $ret = 1; if (true) { $ret = $ret+1;$ret = $ret+1 }", 3f)
        floatProp.assertExpressionReturns("var $ret = 1; if (true) { $ret = $ret+1\n$ret +=1 }", 3f)

        floatProp.assertExpressionReturns(
            "var $ret = 1; if (false) { $ret = $ret } else { $ret = $ret+1 }",
            2f
        )
        floatProp.assertExpressionReturns("var $ret = 1.0; if (false) $ret +=1", 1f)

        floatProp.assertExpressionReturns(
            "var $ret = 1; if (1 != 1) $ret = 0 else { $ret = $ret+1;$ret = $ret+1 }",
            3f
        )
        floatProp.assertExpressionReturns(
            "var $ret = 1; if (!(1 == 1)) $ret = 0 else { $ret +=1;$ret = $ret+1 }",
            3f
        )

        floatProp.assertExpressionReturns("var $ret = 0; if (true) { if (true) { $ret +=1 } }", 1f)
        floatProp.assertExpressionReturns(
            "var $ret = 0; if (true) { if (false) { $ret = 0 } else { $ret += 1 } }",
            1f
        )
        floatProp.assertExpressionReturns(
            "var $ret = 0; if (true) if (false) $ret = 0 else $ret += 1 ",
            1f
        )
    }
}
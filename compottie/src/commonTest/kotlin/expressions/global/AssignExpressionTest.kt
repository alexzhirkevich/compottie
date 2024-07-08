package expressions.global

import expressions.assertExprReturns
import expressions.ret
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class AssignExpressionTest {

    @Test
    fun add_sub_mull_div_assign() {

        "var $ret = 13; $ret += 17".assertExprReturns(30f)
        "var $ret = 56; $ret -=17".assertExprReturns(39f)
        "var $ret = 5; $ret *=2".assertExprReturns(10f)
        "var $ret = 13; $ret *= -2*2".assertExprReturns(-52f)
        "var $ret = 144; $ret /=6".assertExprReturns(24f)

        "var $ret = []; $ret[0] = 5; $ret[1] = 10; $ret[(5-5)] += 10-3; $ret[5-4] += (4*2);"
            .assertExprReturns(Vec2(12f, 18f))

        "var $ret = []\n $ret[0] = 5\n $ret[1] = 10\n $ret[(5-5)] += 10-3\n $ret[5-4] += (4*2);"
            .assertExprReturns(Vec2(12f, 18f))
    }
}

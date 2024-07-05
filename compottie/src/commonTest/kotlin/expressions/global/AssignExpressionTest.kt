package expressions.global

import expressions.ret
import expressions.testExpression
import expressions.testValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class AssignExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)
    private val vecProp = AnimatedNumber.Default(0f)

    @Test
    fun add_sub_mull_div_assign() {

        floatProp.testExpression("var $ret = 13; $ret += 17", 30f)
        floatProp.testExpression("var $ret = 56; $ret -=17", 39f)
        floatProp.testExpression("var $ret = 5; $ret *=2", 10f)
        floatProp.testExpression("var $ret = 13; $ret *= -2*2", -52f)
        floatProp.testExpression("var $ret = 144; $ret /=6", 24f)

        vecProp.testExpression(
            "var $ret = []; $ret[0] = 5; $ret[1] = 10; $ret[(5-5)] += 10-3; $ret[5-4] += (4*2);",
            Vec2(12f, 18f)
        )

        vecProp.testExpression(
            "var $ret = []\n $ret[0] = 5\n $ret[1] = 10\n $ret[(5-5)] += 10-3\n $ret[5-4] += (4*2);",
            Vec2(12f, 18f)
        )

    }
}

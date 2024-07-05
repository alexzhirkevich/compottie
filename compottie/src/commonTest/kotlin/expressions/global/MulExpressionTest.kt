package expressions.global

import expressions.testValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class MulExpressionTest {

    private val vecProp = AnimatedVector2.Default(listOf(0f, 0f))
    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun mul_num_expr() {

        floatProp.testValue("13 * 17.0", 221f)
        floatProp.testValue("-13 * -17", 221f)
        floatProp.testValue("-13.0 * 17 * 2", -442f)

        floatProp.testValue("mul(13, 17)", 221f)
        floatProp.testValue("\$bm_mul(13, 17)", 221f)
        floatProp.testValue("mul(-13, 17) * 2", -442f)
    }

    @Test
    fun mul_vec_num_expr() {

        vecProp.testValue("[13, 17] * 2;", Vec2(26f, 34f))
        vecProp.testValue("[-13, 17] * 2;", Vec2(-26f, 34f))

        vecProp.testValue("mul([13, 17], 2)", Vec2(26f, 34f))
        vecProp.testValue("mul(2,[13, 17])", Vec2(26f, 34f))
        vecProp.testValue("\$bm_mul([13, 17], 2)", Vec2(26f, 34f))
        vecProp.testValue("mul([13, 17], 2) * 2", Vec2(52f, 68f))
    }

    @Test
    fun mul_sum_order() {
        floatProp.testValue("2 + 2 * 2", 6f)
        floatProp.testValue("2 - 2 * 2", -2f)
        floatProp.testValue("(2 + 2) * 2", 8f)
        floatProp.testValue("(2 - 3) * 2", -2f)
    }
}

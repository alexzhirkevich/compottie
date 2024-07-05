package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class MulExpressionTest {

    private val vecProp = AnimatedVector2.Default(listOf(0f, 0f))
    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun mul_num_expr() {

        floatProp.assertValueEquals("13 * 17.0", 221f)
        floatProp.assertValueEquals("-13 * -17", 221f)
        floatProp.assertValueEquals("-13.0 * 17 * 2", -442f)

        floatProp.assertValueEquals("mul(13, 17)", 221f)
        floatProp.assertValueEquals("\$bm_mul(13, 17)", 221f)
        floatProp.assertValueEquals("mul(-13, 17) * 2", -442f)
    }

    @Test
    fun mul_vec_num_expr() {

        vecProp.assertValueEquals("[13, 17] * 2;", Vec2(26f, 34f))
        vecProp.assertValueEquals("[-13, 17] * 2;", Vec2(-26f, 34f))

        vecProp.assertValueEquals("mul([13, 17], 2)", Vec2(26f, 34f))
        vecProp.assertValueEquals("mul(2,[13, 17])", Vec2(26f, 34f))
        vecProp.assertValueEquals("\$bm_mul([13, 17], 2)", Vec2(26f, 34f))
        vecProp.assertValueEquals("mul([13, 17], 2) * 2", Vec2(52f, 68f))
    }

    @Test
    fun mul_sum_order() {
        floatProp.assertValueEquals("2 + 2 * 2", 6f)
        floatProp.assertValueEquals("2 - 2 * 2", -2f)
        floatProp.assertValueEquals("(2 + 2) * 2", 8f)
        floatProp.assertValueEquals("(2 - 3) * 2", -2f)
    }
}

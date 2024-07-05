package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class DivExpressionTest {

    private val vecProp = AnimatedVector2.Default(listOf(0f, 0f))
    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun div_num_expr() {

        floatProp.assertValueEquals("26 / 2.0", 13f)
        floatProp.assertValueEquals("-26 / 2.0", -13f)
        floatProp.assertValueEquals("-26 / -2.0", 13f)
        floatProp.assertValueEquals("-52 / -2.0 / 2", 13f)

        floatProp.assertValueEquals("div(26, 2)", 13f)
        floatProp.assertValueEquals("\$bm_div(26, 2)", 13f)
        floatProp.assertValueEquals("div(-52, 2) / 2", -13f)
    }

    @Test
    fun div_vec_num_expr() {

        vecProp.assertValueEquals("[26, 42] / 2;", Vec2(13f, 21f))
        vecProp.assertValueEquals("[-26, 42] / 2;", Vec2(-13f, 21f))

        vecProp.assertValueEquals("div([26, 42], 2)", Vec2(13f, 21f))
        vecProp.assertValueEquals("\$bm_div([26, 42], 2)", Vec2(13f, 21f))
        vecProp.assertValueEquals("div([26, 42], 2) / 2", Vec2(6.5f, 10.5f))
    }

    @Test
    fun div_sum_order() {
        floatProp.assertValueEquals("2 - 2 / 2", 1f)
        floatProp.assertValueEquals("2 + 2 / 2", 3f)
        floatProp.assertValueEquals("(2 + 4) / 2", 3f)
        floatProp.assertValueEquals("(6 - 2) * 2", 8f)
    }
}

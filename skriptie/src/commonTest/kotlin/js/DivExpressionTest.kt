package js

import expressions.assertExprEquals
import expressions.assertSimpleExprEquals
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


class DivExpressionTest {


    @Test
    fun div_num_expr() {
        "26 / 2.0".assertExprEquals(13f)
        "-26 / 2.0".assertExprEquals(-13f)
        "-26 / -2.0".assertExprEquals(13f)
        "-52 / -2.0 / 2".assertExprEquals(13f)
        "div(26, 2)".assertExprEquals(13f)
        "\$bm_div(26, 2)".assertExprEquals(13f)
        "div(-52, 2) / 2".assertExprEquals(-13f)

        "'15'/'3'".assertExprEquals(5f)
        "15/'3'".assertExprEquals(5f)
        "'15'/3".assertExprEquals(5f)
    }

    @Test
    fun div_vec_num_expr() {
        "[26, 42] / 2;".assertExprEquals(Vec2(13f, 21f))
        "[-26, 42] / 2;".assertExprEquals(Vec2(-13f, 21f))
        "div([26, 42], 2)".assertExprEquals(Vec2(13f, 21f))
        "\$bm_div([26, 42], 2)".assertExprEquals(Vec2(13f, 21f))
        "div([26, 42], 2) / 2".assertExprEquals(Vec2(6.5f, 10.5f))
    }

    @Test
    fun div_sum_order() {
        "2 - 2 / 2".assertExprEquals(1L)
        "2 + 2 / 2".assertExprEquals(3L)
        "(2 + 4) / 2".assertExprEquals(3L)
        "(6 - 2) * 2".assertExprEquals(8L)
    }

    @Test
    fun number_type_propagation() {
        "4/2".assertSimpleExprEquals(2L)
        "4/2.0".assertSimpleExprEquals(2.0)
        "4.0/2".assertSimpleExprEquals(2.0)
        "4.0/2.0".assertSimpleExprEquals(2.0)
    }

    @Test
    fun division_by_zero() {
        "1/0".assertSimpleExprEquals(Double.POSITIVE_INFINITY)
        "1.0/0".assertSimpleExprEquals(Double.POSITIVE_INFINITY)
        "1/0.0".assertSimpleExprEquals(Double.POSITIVE_INFINITY)
        "1.0/0.0".assertSimpleExprEquals(Double.POSITIVE_INFINITY)
    }
}

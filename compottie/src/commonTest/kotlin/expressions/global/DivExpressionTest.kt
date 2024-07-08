package expressions.global

import expressions.assertExprValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class DivExpressionTest {


    @Test
    fun div_num_expr() {
        "26 / 2.0".assertExprValueEquals(13f)
        "-26 / 2.0".assertExprValueEquals(-13f)
        "-26 / -2.0".assertExprValueEquals(13f)
        "-52 / -2.0 / 2".assertExprValueEquals(13f)
        "div(26, 2)".assertExprValueEquals(13f)
        "\$bm_div(26, 2)".assertExprValueEquals(13f)
        "div(-52, 2) / 2".assertExprValueEquals(-13f)
    }

    @Test
    fun div_vec_num_expr() {
        "[26, 42] / 2;".assertExprValueEquals(Vec2(13f, 21f))
        "[-26, 42] / 2;".assertExprValueEquals(Vec2(-13f, 21f))
        "div([26, 42], 2)".assertExprValueEquals(Vec2(13f, 21f))
        "\$bm_div([26, 42], 2)".assertExprValueEquals(Vec2(13f, 21f))
        "div([26, 42], 2) / 2".assertExprValueEquals(Vec2(6.5f, 10.5f))
    }

    @Test
    fun div_sum_order() {
        "2 - 2 / 2".assertExprValueEquals(1f)
        "2 + 2 / 2".assertExprValueEquals(3f)
        "(2 + 4) / 2".assertExprValueEquals(3f)
        "(6 - 2) * 2".assertExprValueEquals(8f)
    }
}

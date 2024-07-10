package expressions.global

import expressions.assertExprValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class MulExpressionTest {


    @Test
    fun mul_num_expr() {
        "13 * 17.0".assertExprValueEquals(221f)
        "-13 * -17".assertExprValueEquals(221f)
        "-13.0 * 17 * 2".assertExprValueEquals(-442f)
        "mul(13, 17)".assertExprValueEquals(221f)
        "\$bm_mul(13, 17)".assertExprValueEquals(221f)
        "mul(-13, 17) * 2".assertExprValueEquals(-442f)
    }

    @Test
    fun mul_vec_num_expr() {
        "[13, 17] * 2;".assertExprValueEquals(Vec2(26f, 34f))
        "[-13, 17] * 2;".assertExprValueEquals(Vec2(-26f, 34f))
        "mul([13, 17], 2)".assertExprValueEquals(Vec2(26f, 34f))
        "mul(2,[13, 17])".assertExprValueEquals(Vec2(26f, 34f))
        "\$bm_mul([13, 17], 2)".assertExprValueEquals(Vec2(26f, 34f))
        "mul([13, 17], 2) * 2".assertExprValueEquals(Vec2(52f, 68f))
    }

    @Test
    fun mul_sum_order() {
        "2 + 2 * 2".assertExprValueEquals(6f)
        "2 - 2 * 2".assertExprValueEquals(-2f)
        "(2 + 2) * 2".assertExprValueEquals(8f)
        "(2 - 3) * 2".assertExprValueEquals(-2f)
    }
}
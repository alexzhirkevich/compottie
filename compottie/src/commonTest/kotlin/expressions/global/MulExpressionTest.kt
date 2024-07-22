package expressions.global

import expressions.assertExprEquals
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class MulExpressionTest {


    @Test
    fun mul_num_expr() {
        "13 * 17.0".assertExprEquals(221f)
        "-13 * -17".assertExprEquals(221f)
        "-13.0 * 17 * 2".assertExprEquals(-442f)
        "mul(13, 17)".assertExprEquals(221f)
        "\$bm_mul(13, 17)".assertExprEquals(221f)
        "mul(-13, 17) * 2".assertExprEquals(-442f)

        "'10'*'3'".assertExprEquals(30f)
        "10*'3'".assertExprEquals(30f)
        "'10'*3".assertExprEquals(30f)
    }

    @Test
    fun mul_vec_num_expr() {
        "[13, 17] * 2;".assertExprEquals(Vec2(26f, 34f))
        "[-13, 17] * 2;".assertExprEquals(Vec2(-26f, 34f))
        "mul([13, 17], 2)".assertExprEquals(Vec2(26f, 34f))
        "mul(2,[13, 17])".assertExprEquals(Vec2(26f, 34f))
        "\$bm_mul([13, 17], 2)".assertExprEquals(Vec2(26f, 34f))
        "mul([13, 17], 2) * 2".assertExprEquals(Vec2(52f, 68f))
    }

    @Test
    fun mul_sum_order() {
        "2 + 2 * 2".assertExprEquals(6f)
        "2 - 2 * 2".assertExprEquals(-2f)
        "(2 + 2) * 2".assertExprEquals(8f)
        "(2 - 3) * 2".assertExprEquals(-2f)
    }
}
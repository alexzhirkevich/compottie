package expressions.global

import expressions.assertExprValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test

internal class SubExpressionTest {


    @Test
    fun sub_num_expr() {
        "13-17".assertExprValueEquals(-4f)
        "13 - 17".assertExprValueEquals(-4f)
        "-13-17".assertExprValueEquals(-30f)
        "13.0-17.0".assertExprValueEquals(-4f)
        "13 - 17.0".assertExprValueEquals(-4f)
        "-13.0 -17".assertExprValueEquals(-30f)
    }

    @Test
    fun sub_vec_expr() {
        "[13, 17] + [17, 13];".assertExprValueEquals(Vec2(30f, 30f))
        "[-13, 17] + [-17, 13];".assertExprValueEquals(Vec2(-30f, 30f))
        "[13.0, 17.0] + [17, 13];".assertExprValueEquals(Vec2(30f, 30f))
        "[-13, 17.0] + [-17.0, 13];".assertExprValueEquals(Vec2(-30f, 30f))
    }

    @Test
    fun sub_num_fun() {
        "sub(13,17)".assertExprValueEquals(-4f)
        "sub(13, 17)".assertExprValueEquals(-4f)
        "sub(-13, 17)".assertExprValueEquals(-30f)
        "sub(13.0,17.0)".assertExprValueEquals(-4f)
        "sub(13 , 17.0)".assertExprValueEquals(-4f)
        "sub(-13.0 ,17)".assertExprValueEquals(-30f)
    }

    @Test
    fun sub_num_fun2() {
        "\$bm_sub(13,17)".assertExprValueEquals(-4f)
        "\$bm_sub(13, 17)".assertExprValueEquals(-4f)
        "\$bm_sub(-13, 17)".assertExprValueEquals(-30f)
        "\$bm_sub(13.0,17.0)".assertExprValueEquals(-4f)
        "\$bm_sub(13 , 17.0)".assertExprValueEquals(-4f)
        "\$bm_sub(-13.0 ,17)".assertExprValueEquals(-30f)
    }

    @Test
    fun sub_vec_fun() {
        "sub([13, 17] , [17, 13]);".assertExprValueEquals(Vec2(-4f, 4f))
        "sub([-13, 17] , [-17, 13]);".assertExprValueEquals(Vec2(4f, 4f))
        "sub([13.0, 17.0] , [17, 13]);".assertExprValueEquals(Vec2(-4f, 4f))
        "sub([-13, 17.0] , [-17.0, 13]);".assertExprValueEquals(Vec2(4f, 4f))

        "sub(vec2=[20.0, 15], vec1=[10, 10]);".assertExprValueEquals(Vec2(-10f, -5f))
    }
}


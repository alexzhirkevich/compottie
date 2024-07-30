package js

import expressions.assertExprEquals
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test

class SubExpressionTest {


    @Test
    fun sub_num_expr() {
        "13-17".assertExprEquals(-4f)
        "13 - 17".assertExprEquals(-4f)
        "-13-17".assertExprEquals(-30f)
        "13.0-17.0".assertExprEquals(-4f)
        "13 - 17.0".assertExprEquals(-4f)
        "-13.0 -17".assertExprEquals(-30f)

        "'10'-'3'".assertExprEquals(7f)
        "10-'3'".assertExprEquals(7f)
        "'10'-3".assertExprEquals(7f)
    }

    @Test
    fun sub_vec_expr() {
        "[13, 17] + [17, 13];".assertExprEquals(Vec2(30f, 30f))
        "[-13, 17] + [-17, 13];".assertExprEquals(Vec2(-30f, 30f))
        "[13.0, 17.0] + [17, 13];".assertExprEquals(Vec2(30f, 30f))
        "[-13, 17.0] + [-17.0, 13];".assertExprEquals(Vec2(-30f, 30f))
    }

    @Test
    fun sub_num_fun() {
        "sub(13,17)".assertExprEquals(-4f)
        "sub(13, 17)".assertExprEquals(-4f)
        "sub(-13, 17)".assertExprEquals(-30f)
        "sub(13.0,17.0)".assertExprEquals(-4f)
        "sub(13 , 17.0)".assertExprEquals(-4f)
        "sub(-13.0 ,17)".assertExprEquals(-30f)
    }

    @Test
    fun sub_num_fun2() {
        "\$bm_sub(13,17)".assertExprEquals(-4f)
        "\$bm_sub(13, 17)".assertExprEquals(-4f)
        "\$bm_sub(-13, 17)".assertExprEquals(-30f)
        "\$bm_sub(13.0,17.0)".assertExprEquals(-4f)
        "\$bm_sub(13 , 17.0)".assertExprEquals(-4f)
        "\$bm_sub(-13.0 ,17)".assertExprEquals(-30f)
    }

    @Test
    fun sub_vec_fun() {
        "sub([13, 17] , [17, 13]);".assertExprEquals(Vec2(-4f, 4f))
        "sub([-13, 17] , [-17, 13]);".assertExprEquals(Vec2(4f, 4f))
        "sub([13.0, 17.0] , [17, 13]);".assertExprEquals(Vec2(-4f, 4f))
        "sub([-13, 17.0] , [-17.0, 13]);".assertExprEquals(Vec2(4f, 4f))

        "sub(vec2=[20.0, 15], vec1=[10, 10]);".assertExprEquals(Vec2(-10f, -5f))
    }
}


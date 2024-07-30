package js

import expressions.assertExprEquals
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


class AddExpressionTest {

    @Test
    fun add_num_expr() {
        "13+17".assertExprEquals(30f)
        "-13+ 17".assertExprEquals(4f)
        "-13+ -17".assertExprEquals(-30f)
        "-13+ -17.0 + 10 - 4.0".assertExprEquals(-24f)

        "'10'+'5'".assertExprEquals("105")
        "'10'+5".assertExprEquals("105")
        "10+'5'".assertExprEquals("105")
    }

    @Test
    fun add_vec_expr(){
        "[13, 17] + [17, 13];".assertExprEquals(Vec2(30f,30f))
        "[-13, 17] + [-17, 13];".assertExprEquals(Vec2(-30f,30f))
        "[13.0, 17.0] + [17, 13];".assertExprEquals(Vec2(30f,30f))
        "[-13, 17.0] + [-17.0, 13];".assertExprEquals(Vec2(-30f,30f))
    }

    @Test
    fun add_num_vec() {
        "[10,20] + 5".assertExprEquals(15f)
        "5 +[10,20]".assertExprEquals(15f)
        "sum([10,20], 5)".assertExprEquals(15f)
        "sum(5, [10,20])".assertExprEquals(15f)
        "\$bm_sum([10,20], 5)".assertExprEquals(15f)
        "\$bm_sum(5 ,[10,20])".assertExprEquals(15f)
    }

    @Test
    fun add_num_fun() {
        "add(13,17)".assertExprEquals(30f)
        " add( 13, 17); ".assertExprEquals(30f)
        "add(-13,   17)  ".assertExprEquals(4f)
        "add(+13,-17)".assertExprEquals(-4f)
        "add( 13, 17.0)".assertExprEquals(30f)
        "add(13, 17.0)".assertExprEquals(30f)
        " add(-13.0, 17);".assertExprEquals(4f)
        "add(-13.0, -17.0)  ".assertExprEquals(-30f)
    }

    @Test
    fun add_num_fun2() {
        "sum(13,17)".assertExprEquals(30f)
        " sum( 13, 17); ".assertExprEquals(30f)
        "sum(-13,   17)  ".assertExprEquals(4f)
        "sum(+13,-17)".assertExprEquals(-4f)
        "sum( 13, 17.0)".assertExprEquals(30f)
        "sum(13, 17.0)".assertExprEquals(30f)
        " sum(-13.0, 17);".assertExprEquals(4f)
        "sum(-13.0, -17.0)  ".assertExprEquals(-30f)
    }

    @Test
    fun add_num_fun3() {
        "\$bm_sum(13,17)".assertExprEquals(30f)
        " \$bm_sum( 13, 17); ".assertExprEquals(30f)
        "\$bm_sum(-13,   17)  ".assertExprEquals(4f)
        "\$bm_sum(+13,-17)".assertExprEquals(-4f)
        "\$bm_sum( 13, 17.0)".assertExprEquals(30f)
        "\$bm_sum(13, 17.0)".assertExprEquals(30f)
        " \$bm_sum(-13.0, 17);".assertExprEquals(4f)
        "\$bm_sum(-13.0, -17.0)  ".assertExprEquals(-30f)
    }


    @Test
    fun add_vec_fun2(){
        "sum([13, 17], [17, 13]);".assertExprEquals(Vec2(30f,30f))
        "sum([-13, 17], [-17, 13]);".assertExprEquals(Vec2(-30f,30f))
        "sum([13.0, 17.0], [17, 13]);".assertExprEquals(Vec2(30f,30f))
        "sum([-13, 17.0], [-17.0, 13]);".assertExprEquals(Vec2( -30f,30f))
    }

    @Test
    fun add_vec_fun3(){
        "\$bm_sum([13, 17], [17, 13]);".assertExprEquals(Vec2(30f,30f))
        "\$bm_sum([-13, 17], [-17, 13]);".assertExprEquals(Vec2(-30f,30f))
        "\$bm_sum([13.0, 17.0], [17, 13]);".assertExprEquals(Vec2(30f,30f))
        "\$bm_sum([-13, 17.0], [-17.0, 13]);".assertExprEquals(Vec2(-30f,30f))
    }
}


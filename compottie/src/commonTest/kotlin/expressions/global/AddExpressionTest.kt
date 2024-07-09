package expressions.global

import expressions.assertExprValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class AddExpressionTest {

    @Test
    fun add_num_expr() {
        "13+17".assertExprValueEquals(30f)
        "-13+ 17".assertExprValueEquals(4f)
        "-13+ -17".assertExprValueEquals(-30f)
        "-13+ -17.0 + 10 - 4.0".assertExprValueEquals(-24f)
    }

    @Test
    fun add_vec_expr(){
        "[13, 17] + [17, 13];".assertExprValueEquals(Vec2(30f,30f))
        "[-13, 17] + [-17, 13];".assertExprValueEquals(Vec2(-30f,30f))
        "[13.0, 17.0] + [17, 13];".assertExprValueEquals(Vec2(30f,30f))
        "[-13, 17.0] + [-17.0, 13];".assertExprValueEquals(Vec2(-30f,30f))
    }

    @Test
    fun add_num_vec() {
        "[10,20] + 5".assertExprValueEquals(15f)
        "5 +[10,20]".assertExprValueEquals(15f)
        "sum([10,20], 5)".assertExprValueEquals(15f)
        "sum(5, [10,20])".assertExprValueEquals(15f)
        "\$bm_sum([10,20], 5)".assertExprValueEquals(15f)
        "\$bm_sum(5 ,[10,20])".assertExprValueEquals(15f)
    }

    @Test
    fun add_num_fun() {
        "add(13,17)".assertExprValueEquals(30f)
        " add( 13, 17); ".assertExprValueEquals(30f)
        "add(-13,   17)  ".assertExprValueEquals(4f)
        "add(+13,-17)".assertExprValueEquals(-4f)
        "add( 13, 17.0)".assertExprValueEquals(30f)
        "add(13, 17.0)".assertExprValueEquals(30f)
        " add(-13.0, 17);".assertExprValueEquals(4f)
        "add(-13.0, -17.0)  ".assertExprValueEquals(-30f)


    }

    @Test
    fun add_num_fun2() {
        "sum(13,17)".assertExprValueEquals(30f)
        " sum( 13, 17); ".assertExprValueEquals(30f)
        "sum(-13,   17)  ".assertExprValueEquals(4f)
        "sum(+13,-17)".assertExprValueEquals(-4f)
        "sum( 13, 17.0)".assertExprValueEquals(30f)
        "sum(13, 17.0)".assertExprValueEquals(30f)
        " sum(-13.0, 17);".assertExprValueEquals(4f)
        "sum(-13.0, -17.0)  ".assertExprValueEquals(-30f)
    }

    @Test
    fun add_num_fun3() {
        "\$bm_sum(13,17)".assertExprValueEquals(30f)
        " \$bm_sum( 13, 17); ".assertExprValueEquals(30f)
        "\$bm_sum(-13,   17)  ".assertExprValueEquals(4f)
        "\$bm_sum(+13,-17)".assertExprValueEquals(-4f)
        "\$bm_sum( 13, 17.0)".assertExprValueEquals(30f)
        "\$bm_sum(13, 17.0)".assertExprValueEquals(30f)
        " \$bm_sum(-13.0, 17);".assertExprValueEquals(4f)
        "\$bm_sum(-13.0, -17.0)  ".assertExprValueEquals(-30f)
    }


    @Test
    fun add_vec_fun2(){
        "sum([13, 17], [17, 13]);".assertExprValueEquals(Vec2(30f,30f))
        "sum([-13, 17], [-17, 13]);".assertExprValueEquals(Vec2(-30f,30f))
        "sum([13.0, 17.0], [17, 13]);".assertExprValueEquals(Vec2(30f,30f))
        "sum([-13, 17.0], [-17.0, 13]);".assertExprValueEquals(Vec2( -30f,30f))
    }

    @Test
    fun add_vec_fun3(){
        "\$bm_sum([13, 17], [17, 13]);".assertExprValueEquals(Vec2(30f,30f))
        "\$bm_sum([-13, 17], [-17, 13]);".assertExprValueEquals(Vec2(-30f,30f))
        "\$bm_sum([13.0, 17.0], [17, 13]);".assertExprValueEquals(Vec2(30f,30f))
        "\$bm_sum([-13, 17.0], [-17.0, 13]);".assertExprValueEquals(Vec2(-30f,30f))
    }
}


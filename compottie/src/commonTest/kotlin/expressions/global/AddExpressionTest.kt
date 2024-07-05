package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class AddExpressionTest {

    private val vecProp = AnimatedVector2.Default(listOf(0f,0f))
    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun add_num_expr() {

        floatProp.assertValueEquals("13+17", 13 + 17f)
        floatProp.assertValueEquals("13 + 17", 13 + 17f)
        floatProp.assertValueEquals("-13+ 17", -13 + 17f)
        floatProp.assertValueEquals("-13+ -17", -13 + -17f)
        floatProp.assertValueEquals("-13+ -17 + 10 - 4", -13 + -17 + 10 - 4f)

        floatProp.assertValueEquals("13+17.0", 13+17.0f)
        floatProp.assertValueEquals("13 + 17.0", 13 + 17.0f)
        floatProp.assertValueEquals("-13.0+ 17", -13.0f+ 17f)
        floatProp.assertValueEquals("-13.0+ -17.0", -13.0f+ -17.0f)
    }

    @Test
    fun add_vec_expr(){

        vecProp.assertValueEquals("[13, 17] + [17, 13];", Vec2(13f,17f) + Vec2(17f,13f))
        vecProp.assertValueEquals("[-13, 17] + [-17, 13];", Vec2(-13f,17f) + Vec2(-17f,13f))

        vecProp.assertValueEquals("[13.0, 17.0] + [17, 13];", Vec2(13f,17f) + Vec2(17f,13f))
        vecProp.assertValueEquals("[-13, 17.0] + [-17.0, 13];", Vec2(-13f,17f) + Vec2(-17f,13f))
    }

    @Test
    fun add_num_vec() {

        floatProp.assertValueEquals("[10,20] + 5", 15f)
        floatProp.assertValueEquals("5 +[10,20]", 15f)

        floatProp.assertValueEquals("sum([10,20], 5)", 15f)
        floatProp.assertValueEquals("sum(5, [10,20])", 15f)

        floatProp.assertValueEquals("\$bm_sum([10,20], 5)", 15f)
        floatProp.assertValueEquals("\$bm_sum(5 ,[10,20])", 15f)
    }

    @Test
    fun add_num_fun() {

        floatProp.assertValueEquals("add(13,17)", 30f)
        floatProp.assertValueEquals(" add( 13, 17); ", 30f)
        floatProp.assertValueEquals("add(-13,   17)  ", 4f)
        floatProp.assertValueEquals("add(+13,-17)", -4f)

        floatProp.assertValueEquals("add( 13, 17.0)", 30f)
        floatProp.assertValueEquals("add(13, 17.0)", 30f)
        floatProp.assertValueEquals(" add(-13.0, 17);", 4f)
        floatProp.assertValueEquals("add(-13.0, -17.0)  ", -30f)
    }

    @Test
    fun add_num_fun2() {

        floatProp.assertValueEquals("sum(13,17)", 30f)
        floatProp.assertValueEquals(" sum( 13, 17); ", 30f)
        floatProp.assertValueEquals("sum(-13,   17)  ", 4f)
        floatProp.assertValueEquals("sum(+13,-17)", -4f)

        floatProp.assertValueEquals("sum( 13, 17.0)", 30f)
        floatProp.assertValueEquals("sum(13, 17.0)", 30f)
        floatProp.assertValueEquals(" sum(-13.0, 17);", 4f)
        floatProp.assertValueEquals("sum(-13.0, -17.0)  ", -30f)
    }

    @Test
    fun add_num_fun3() {

        floatProp.assertValueEquals("\$bm_sum(13,17)", 30f)
        floatProp.assertValueEquals(" \$bm_sum( 13, 17); ", 30f)
        floatProp.assertValueEquals("\$bm_sum(-13,   17)  ", 4f)
        floatProp.assertValueEquals("\$bm_sum(+13,-17)", -4f)

        floatProp.assertValueEquals("\$bm_sum( 13, 17.0)", 30f)
        floatProp.assertValueEquals("\$bm_sum(13, 17.0)", 30f)
        floatProp.assertValueEquals(" \$bm_sum(-13.0, 17);", 4f)
        floatProp.assertValueEquals("\$bm_sum(-13.0, -17.0)  ", -30f)
    }


    @Test
    fun add_vec_fun2(){

        vecProp.assertValueEquals("sum([13, 17], [17, 13]);", Vec2(30f,30f))
        vecProp.assertValueEquals("sum([-13, 17], [-17, 13]);", Vec2(-30f,30f))

        vecProp.assertValueEquals("sum([13.0, 17.0], [17, 13]);", Vec2(30f,30f))
        vecProp.assertValueEquals("sum([-13, 17.0], [-17.0, 13]);", Vec2(-30f,30f))
    }

    @Test
    fun add_vec_fun3(){

        vecProp.assertValueEquals("\$bm_sum([13, 17], [17, 13]);", Vec2(30f,30f))
        vecProp.assertValueEquals("\$bm_sum([-13, 17], [-17, 13]);", Vec2(-30f,30f))

        vecProp.assertValueEquals("\$bm_sum([13.0, 17.0], [17, 13]);", Vec2(30f,30f))
        vecProp.assertValueEquals("\$bm_sum([-13, 17.0], [-17.0, 13]);", Vec2(-30f,30f))
    }
}


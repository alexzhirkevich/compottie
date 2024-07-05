package expressions.global

import expressions.testValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class AddExpressionTest {

    private val vecProp = AnimatedVector2.Default(listOf(0f,0f))
    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun add_num_expr() {

        floatProp.testValue("13+17", 30f)
        floatProp.testValue("13 + 17", 30f)
        floatProp.testValue("-13+ 17", 4f)
        floatProp.testValue("-13+ -17", -30f)
        floatProp.testValue("-13+ -17 + 10 - 4", -24f)

        floatProp.testValue("13+17.0", 30f)
        floatProp.testValue("13 + 17.0", 30f)
        floatProp.testValue("-13.0+ 17", 4f)
        floatProp.testValue("-13.0+ -17.0", -30f)
    }

    @Test
    fun add_vec_expr(){

        vecProp.testValue("[13, 17] + [17, 13];", Vec2(30f,30f))
        vecProp.testValue("[-13, 17] + [-17, 13];", Vec2(-30f,30f))

        vecProp.testValue("[13.0, 17.0] + [17, 13];", Vec2(30f,30f))
        vecProp.testValue("[-13, 17.0] + [-17.0, 13];", Vec2(-30f,30f))
    }

    @Test
    fun add_num_vec() {

        floatProp.testValue("[10,20] + 5", 15f)
        floatProp.testValue("5 +[10,20]", 15f)

        floatProp.testValue("sum([10,20], 5)", 15f)
        floatProp.testValue("sum(5, [10,20])", 15f)

        floatProp.testValue("\$bm_sum([10,20], 5)", 15f)
        floatProp.testValue("\$bm_sum(5 ,[10,20])", 15f)
    }

    @Test
    fun add_num_fun() {

        floatProp.testValue("add(13,17)", 30f)
        floatProp.testValue(" add( 13, 17); ", 30f)
        floatProp.testValue("add(-13,   17)  ", 4f)
        floatProp.testValue("add(+13,-17)", -4f)

        floatProp.testValue("add( 13, 17.0)", 30f)
        floatProp.testValue("add(13, 17.0)", 30f)
        floatProp.testValue(" add(-13.0, 17);", 4f)
        floatProp.testValue("add(-13.0, -17.0)  ", -30f)
    }

    @Test
    fun add_num_fun2() {

        floatProp.testValue("sum(13,17)", 30f)
        floatProp.testValue(" sum( 13, 17); ", 30f)
        floatProp.testValue("sum(-13,   17)  ", 4f)
        floatProp.testValue("sum(+13,-17)", -4f)

        floatProp.testValue("sum( 13, 17.0)", 30f)
        floatProp.testValue("sum(13, 17.0)", 30f)
        floatProp.testValue(" sum(-13.0, 17);", 4f)
        floatProp.testValue("sum(-13.0, -17.0)  ", -30f)
    }

    @Test
    fun add_num_fun3() {

        floatProp.testValue("\$bm_sum(13,17)", 30f)
        floatProp.testValue(" \$bm_sum( 13, 17); ", 30f)
        floatProp.testValue("\$bm_sum(-13,   17)  ", 4f)
        floatProp.testValue("\$bm_sum(+13,-17)", -4f)

        floatProp.testValue("\$bm_sum( 13, 17.0)", 30f)
        floatProp.testValue("\$bm_sum(13, 17.0)", 30f)
        floatProp.testValue(" \$bm_sum(-13.0, 17);", 4f)
        floatProp.testValue("\$bm_sum(-13.0, -17.0)  ", -30f)
    }


    @Test
    fun add_vec_fun2(){

        vecProp.testValue("sum([13, 17], [17, 13]);", Vec2(30f,30f))
        vecProp.testValue("sum([-13, 17], [-17, 13]);", Vec2(-30f,30f))

        vecProp.testValue("sum([13.0, 17.0], [17, 13]);", Vec2(30f,30f))
        vecProp.testValue("sum([-13, 17.0], [-17.0, 13]);", Vec2(-30f,30f))
    }

    @Test
    fun add_vec_fun3(){

        vecProp.testValue("\$bm_sum([13, 17], [17, 13]);", Vec2(30f,30f))
        vecProp.testValue("\$bm_sum([-13, 17], [-17, 13]);", Vec2(-30f,30f))

        vecProp.testValue("\$bm_sum([13.0, 17.0], [17, 13]);", Vec2(30f,30f))
        vecProp.testValue("\$bm_sum([-13, 17.0], [-17.0, 13]);", Vec2(-30f,30f))
    }
}


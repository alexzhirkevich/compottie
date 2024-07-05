package expressions.global

import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test

internal class SubExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)
    private val vecProp = AnimatedVector2.Default(listOf(0f,0f))

    @Test
    fun sub_num_expr() {

        floatProp.assertValueEquals("13-17", -4f)
        floatProp.assertValueEquals("13 - 17", -4f)
        floatProp.assertValueEquals("-13-17", -30f)

        floatProp.assertValueEquals("13.0-17.0", -4f)
        floatProp.assertValueEquals("13 - 17.0", -4f)
        floatProp.assertValueEquals("-13.0 -17", -30f)
    }

    @Test
    fun sub_vec_expr(){

        vecProp.assertValueEquals("[13, 17] + [17, 13];", Vec2(30f,30f))
        vecProp.assertValueEquals("[-13, 17] + [-17, 13];", Vec2(-30f,30f))

        vecProp.assertValueEquals("[13.0, 17.0] + [17, 13];", Vec2(30f,30f))
        vecProp.assertValueEquals("[-13, 17.0] + [-17.0, 13];", Vec2(-30f,30f))
    }

    @Test
    fun sub_num_fun() {
        floatProp.assertValueEquals("sub(13,17)", -4f)
        floatProp.assertValueEquals("sub(13, 17)", -4f)
        floatProp.assertValueEquals("sub(-13, 17)", -30f)

        floatProp.assertValueEquals("sub(13.0,17.0)", -4f)
        floatProp.assertValueEquals("sub(13 , 17.0)", -4f)
        floatProp.assertValueEquals("sub(-13.0 ,17)", -30f)
    }

    @Test
    fun sub_num_fun2() {
        floatProp.assertValueEquals("\$bm_sub(13,17)", -4f)
        floatProp.assertValueEquals("\$bm_sub(13, 17)", -4f)
        floatProp.assertValueEquals("\$bm_sub(-13, 17)", -30f)

        floatProp.assertValueEquals("\$bm_sub(13.0,17.0)", -4f)
        floatProp.assertValueEquals("\$bm_sub(13 , 17.0)", -4f)
        floatProp.assertValueEquals("\$bm_sub(-13.0 ,17)", -30f)
    }

    @Test
    fun sub_vec_fun(){
        vecProp.assertValueEquals("sub([13, 17] , [17, 13]);", Vec2(-4f,4f))
        vecProp.assertValueEquals("sub([-13, 17] , [-17, 13]);", Vec2(4f,4f))

        vecProp.assertValueEquals("sub([13.0, 17.0] , [17, 13]);", Vec2(-4f,4f))
        vecProp.assertValueEquals("sub([-13, 17.0] , [-17.0, 13]);", Vec2(4f,4f))
    }
}


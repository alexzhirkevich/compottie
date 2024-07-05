package expressions.global

import expressions.assertFloatApproxEquals
import expressions.assertValueEquals
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import kotlin.test.Test


internal class ModExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun mod_num(){
        floatProp.assertValueEquals("mod(25,4)",1f)
        floatProp.assertFloatApproxEquals("mod(25.1,4)",1.1f)
    }
}


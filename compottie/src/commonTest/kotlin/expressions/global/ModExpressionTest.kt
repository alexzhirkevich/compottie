package expressions.global

import expressions.testFloatApprox
import expressions.testValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import kotlin.test.Test


internal class ModExpressionTest {

    private val floatProp = AnimatedNumber.Default(0f)

    @Test
    fun mod_num(){
        floatProp.testValue("mod(25,4)",1f)
        floatProp.testFloatApprox("mod(25.1,4)",1.1f)
    }
}


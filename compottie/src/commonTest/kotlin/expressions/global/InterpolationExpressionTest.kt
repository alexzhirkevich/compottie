package expressions.global

import androidx.compose.animation.core.LinearEasing
import expressions.assertExprEquals
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.easeIn
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.easeInOut
import io.github.alexzhirkevich.compottie.internal.animation.expressions.operations.easeOut
import kotlin.test.Test

internal class InterpolationExpressionTest {

    private val easingTests = mapOf(
        "linear" to LinearEasing,
        "ease" to easeInOut,
        "easeIn" to easeIn,
        "easeOut" to easeOut,
    )

    @Test
    fun easing() {
        easingTests.forEach { (f, e) ->
            "$f(0, 0, 100)".assertExprEquals(0f)
            "$f(0, 0.0, 100)".assertExprEquals(0f)
            "$f(1, 0, 100.0)".assertExprEquals(100f)
            "$f(1, 0, 100)".assertExprEquals(100f)
            "$f(0.5, 0, 100)".assertExprEquals(e.transform(.5f) * 100f)
            "$f(0.5, 0.0, 100)".assertExprEquals(e.transform(.5f) * 100f)
            "$f(1.5, 1, 2, 0, 100)".assertExprEquals(e.transform(.5f) * 100f)
            "$f(1.1, 0,1, 0, 100)".assertExprEquals(100f)
            "$f(1.1, 0,1, 0, 100.0)".assertExprEquals(100f)
            "$f(-0.1, 0,1, 0, 100)".assertExprEquals(0f)
            "$f(-0.1, 0,1, 0.0, 100)".assertExprEquals(0f)
        }
    }
}
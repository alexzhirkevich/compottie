package expressions

import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.internal.Animation
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionEvaluator
import io.github.alexzhirkevich.compottie.internal.layers.NullLayer
import io.github.alexzhirkevich.compottie.mockFontFamilyResolver
import kotlin.test.assertEquals

internal const val ret = "\$bm_rt"


internal  fun < T : Any> RawProperty<T>.assertValueEquals(
    expr : String,
    expected : T,
) = assertExpressionReturns("$ret = $expr", expected)

internal fun RawProperty<Float>.assertFloatApproxEquals(
    expr : String,
    expected : Float,
) {
    val state = MockAnimationState(0f)
    val ev = ExpressionEvaluator<Float>("$ret = $expr")

    val evaluated = ev.run { evaluate(state) } as Float

    assertEquals(expected, (evaluated * 10000).toInt()/10000f)
}


internal fun <T : Any> RawProperty<T>.assertExpressionReturns(
    expr : String,
    expected : T,
) {
    val state = MockAnimationState(0f)
    val ev = ExpressionEvaluator<T>(expr)

    val evaluated = ev.run { evaluate(state) }

    assertEquals(expected, evaluated)
    assertEquals(expected::class, evaluated::class)
}

internal fun MockAnimationState(
    frame : Float,
    durationFrames : Float = 120f
) = AnimationState(
    composition = LottieComposition(
        Animation(
            frameRate = 24f,
            width = 1024f,
            height = 1024f,
            version = "5.0.0",
            inPoint = 0f,
            outPoint = durationFrames,
            name = "Animation"
        )
    ),
    assets = emptyMap(),
    applyOpacityToLayers = false,
    enableExpressions = true,
    fonts = emptyMap(),
    frame = frame,
    fontFamilyResolver = mockFontFamilyResolver(),
    clipToCompositionBounds = true,
    clipTextToBoundingBoxes = true,
    enableMergePaths = true,
    layer = NullLayer()
)
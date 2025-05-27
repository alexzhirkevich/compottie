package io.github.alexzhirkevich.compottie.internal.animation.expressions

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.ExpressionHolder
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.animation.Vec2
import io.github.alexzhirkevich.keight.Script
import io.github.alexzhirkevich.keight.invokeSync

internal interface ExpressionEvaluator : ExpressionHolder {
    fun evaluate(state: AnimationState): Any
}

internal fun ExpressionEvaluator(
    expression: String,
    property: RawProperty<*>
) : ExpressionEvaluator = ExpressionEvaluatorImpl(expression, property)


private class ExpressionEvaluatorImpl(
    private val expr: String,
    private val property: RawProperty<*>
) : ExpressionEvaluator {

    private var script: Script? = null


    override fun prepareExpressions(state: AnimationState) {
        val script = state.scriptEngine.compile(expr)

        this.script = Script { runtime ->
            runtime.withScope(state.thisComp) { compScope ->
                compScope.withScope(state.thisLayer) { layerScope ->
                    layerScope.withScope(property) { propertyScope ->
                        script.invoke(propertyScope)
                    }
                }
            }
        }
    }

    override fun evaluate(state: AnimationState): Any {
        val script = script ?: return property.raw(state)

        return try {
            script.invokeSync(state.scriptEngine.runtime)
                ?.toKotlin(state.scriptEngine.runtime)
        } catch (t: Throwable) {
            null
        } ?: property.raw(state)
    }
}


private fun Any.toListOrThis() : Any{
    return when (this){
        is Map<*,*> -> values.toList()
        is Vec2 -> listOf(x,y)
        is Color -> listOf(red,green,blue,alpha)
        is Array<*> -> toList()
        else -> this
    }
}


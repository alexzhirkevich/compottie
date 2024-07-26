//package io.github.alexzhirkevich.compottie.internal.animation.expressions.operations
//
//import io.github.alexzhirkevich.compottie.internal.AnimationState
//import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
//import io.github.alexzhirkevich.compottie.internal.animation.expressions.EvaluationContext
//import io.github.alexzhirkevich.compottie.internal.animation.expressions.Expression
//import io.github.alexzhirkevich.compottie.internal.animation.expressions.ExpressionContext
//import io.github.alexzhirkevich.compottie.internal.animation.expressions.Undefined
//
//internal class Object(
//    private val properties : Map<String, LazyConstExpression>
//) : ExpressionContext<Any> {
//
//    private val obj = mutableMapOf<String, Any?>()
//
//    override fun interpret(callable: String?, args: List<Expression>?): Expression? {
//        return when {
//            callable == null -> null
//            args == null -> field(callable)
//            else -> func(callable, args)
//        }
//    }
//
//    override fun invoke(
//        property: RawProperty<Any>,
//        context: EvaluationContext,
//        state: AnimationState
//    ): Any = this
//
//    private fun field(name: String): Expression {
//        return Expression { _, _, _ -> obj[name] ?: Undefined }
//    }
//
//    private fun func(name: String, args: List<Expression>): Expression {
//        return Expression { property, context, state ->
//            obj[name]
//        }
//    }
//}
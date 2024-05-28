package io.github.alexzhirkevich.compottie.internal.schema

import io.github.alexzhirkevich.compottie.internal.schema.layers.Layer
import io.github.alexzhirkevich.compottie.internal.schema.layers.ShapeLayer
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Ellipse
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Fill
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientFill
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientStroke
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Group
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Path
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Rect
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Round
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Shape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.SolidStroke
import io.github.alexzhirkevich.compottie.internal.schema.shapes.TransformShape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Trim
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val LottieJson = Json {
    ignoreUnknownKeys = true
    isLenient = true

    serializersModule = SerializersModule {
        polymorphic(Layer::class){
            subclass(ShapeLayer::class)
        }

        polymorphic(AnimatedValue::class){
            subclass(AnimatedValue.Default::class)
            subclass(AnimatedValue.Keyframed::class)
        }
        polymorphic(AnimatedVector2::class){
            subclass(AnimatedVector2.Keyframed::class)
            subclass(AnimatedVector2.Default::class)
        }
        polymorphic(Shape::class){
            subclass(Path::class)
            subclass(Ellipse::class)
            subclass(Fill::class)
            subclass(GradientFill::class)
            subclass(Group::class)
            subclass(Rect::class)
            subclass(Round::class)
            subclass(SolidStroke::class)
            subclass(GradientStroke::class)
            subclass(Trim::class)
            subclass(TransformShape::class)
        }
    }
}
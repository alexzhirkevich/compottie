package io.github.alexzhirkevich.compottie.internal.schema

import io.github.alexzhirkevich.compottie.internal.schema.layers.Layer
import io.github.alexzhirkevich.compottie.internal.schema.layers.ShapeLayer
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Ellipse
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Fill
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientFill
import io.github.alexzhirkevich.compottie.internal.schema.shapes.GradientStroke
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Group
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Path
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Rect
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Round
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Shape
import io.github.alexzhirkevich.compottie.internal.schema.shapes.Stroke
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

        polymorphic(Value::class){
            subclass(Value.Default::class)
            subclass(Value.Keyframed::class)
        }
        polymorphic(Vector::class){
            subclass(Vector.Keyframed::class)
            subclass(Vector.Default::class)
        }
        polymorphic(Shape::class){
            subclass(Path::class)
            subclass(Ellipse::class)
            subclass(Fill::class)
            subclass(GradientFill::class)
            subclass(Group::class)
            subclass(Rect::class)
            subclass(Round::class)
            subclass(Stroke::class)
            subclass(GradientStroke::class)
            subclass(Trim::class)
            subclass(TransformShape::class)
        }
    }
}
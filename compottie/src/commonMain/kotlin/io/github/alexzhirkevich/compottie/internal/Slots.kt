package io.github.alexzhirkevich.compottie.internal

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedGradient
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedShape
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonElement

internal class Slots(
    private val slots : Map<String, JsonElement>
) {
    private val cache = mutableMapOf<String, RawProperty<*>>()

    fun number(sid: String): AnimatedNumber? = property(sid, AnimatedNumber.serializer())
    fun vector(sid: String): AnimatedVector2? = property(sid, AnimatedVector2.serializer())
    fun color(sid: String): AnimatedColor? = property(sid, AnimatedColor.serializer())
    fun gradient(sid: String): AnimatedGradient? = property(sid, AnimatedGradient.serializer())
    fun shape(sid: String) : AnimatedShape? = property(sid, AnimatedShape.serializer())

    private fun <T : RawProperty<*>> property(
        sid : String,
        deserializer : DeserializationStrategy<T>,
    ) : T? {
        println("Slot requested: $sid")
        val json = slots[sid] ?: return null
        val cached = cache[sid]
        if (cached != null){
            return cached as T
        }
        val new = LottieJson.decodeFromJsonElement(deserializer, json)
        cache[sid] = new
        return new
    }
}
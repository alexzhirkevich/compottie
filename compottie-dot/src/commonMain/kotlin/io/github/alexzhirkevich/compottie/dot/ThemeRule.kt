package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.AnimationTheme
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import kotlinx.serialization.Serializable

@Serializable
internal class ThemeRules(
    val rules : List<ThemeRule<*>>
)

internal fun ThemeRules.toTheme() : AnimationTheme {
    return AnimationTheme(
        rules = rules.associate { it.id to it.property() }
    )
}

@Serializable
internal sealed interface ThemeRule<V> {
    val id : String
    val animations : List<String>?
    val expression : String?
    val keyframes : List<DotKeyframe<*>>?
    val value : V?

    fun property() : RawProperty<*>
}
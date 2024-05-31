package io.github.alexzhirkevich.compottie.internal.schema.helpers

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
internal value class BooleanInt(val value : Byte) {

    companion object {
        val Yes = BooleanInt(1)
        val No = BooleanInt(0)
    }

    fun toBoolean() = this == Yes
}
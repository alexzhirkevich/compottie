package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable

@Immutable
sealed class LottieCompositionSpec {

    @Immutable
    class JsonString constructor(
        internal val jsonString: String
    ) : LottieCompositionSpec()  {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as JsonString

            if (jsonString != other.jsonString) return false

            return true
        }

        override fun hashCode(): Int {
            return jsonString.hashCode()
        }

        override fun toString(): String {
            return "JsonString(jsonString='$jsonString')"
        }

    }

     companion object
}


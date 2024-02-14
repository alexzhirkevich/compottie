package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable

@Immutable
actual sealed class LottieCompositionSpec {

    @Immutable
    actual class JsonString actual constructor(
        internal val jsonString: String
    ) : LottieCompositionSpec()  {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as JsonString
            return jsonString == other.jsonString
        }

        override fun hashCode(): Int {
            return jsonString.hashCode()
        }

        override fun toString(): String {
            return "JsonString(jsonString='$jsonString')"
        }

    }

    @Immutable
    actual class Url actual constructor(
        internal val url: String
    ) : LottieCompositionSpec() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Url
            return url == other.url
        }

        override fun hashCode(): Int {
            return url.hashCode()
        }

        override fun toString(): String {
            return "Url(url='$url')"
        }
    }

    actual companion object
}

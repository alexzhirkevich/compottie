package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import kotlin.jvm.JvmInline

@Stable
interface LottieCompositionSpec {

    suspend fun load() : LottieComposition

    companion object {


        /**
        *  [LottieComposition] from a [jsonString]
        */
        @Stable
        @Deprecated(
            "Use overload with lazy loading instead",
            replaceWith = ReplaceWith(
                "JsonString { jsonString }"
            )
        )
        fun JsonString(
            jsonString: String
        ): LottieCompositionSpec = JsonStringImpl(jsonString)

        /**
         * [LottieComposition] from a lazy [jsonString]
         *
         * Lambda should be stable. Otherwise this spec must be remembered if created in composition
         * */
        @Stable
        fun JsonString(
            jsonString: suspend () -> String
        ): LottieCompositionSpec = LazyJsonString(jsonString)
    }
}


@Immutable
@JvmInline
private value class JsonStringImpl(
    private val jsonString: String
) : LottieCompositionSpec  {

    override suspend fun load(): LottieComposition {
        return LottieComposition.parse(jsonString)
    }

    override fun toString(): String {
        return "JsonString(jsonString='$jsonString')"
    }
}

@Immutable
private class LazyJsonString(
    private val jsonString : suspend () -> String
) : LottieCompositionSpec {

    override suspend fun load(): LottieComposition {
        return LottieComposition.parse(jsonString())
    }

    override fun toString(): String {
        return "LazyJsonString(jsonString='$jsonString')"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? LazyJsonString)?.jsonString == jsonString
    }

    override fun hashCode(): Int {
        return jsonString.hashCode()
    }


}



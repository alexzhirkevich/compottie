package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@Stable
interface LottieCompositionSpec {

    suspend fun load(cacheKey : Any? = null) : LottieComposition

    companion object {


        /**
        *  [LottieComposition] from a [jsonString]
        */
        @Stable
        fun JsonString(
            jsonString: String
        ): LottieCompositionSpec = JsonStringImpl(jsonString)
    }
}


@Immutable
@JvmInline
private value class JsonStringImpl(
    private val jsonString: String
) : LottieCompositionSpec {

    override suspend fun load(cacheKey: Any?): LottieComposition {
        return LottieComposition.getOrCreate(cacheKey) {
            LottieComposition.parse(jsonString)
        }
    }

    override fun toString(): String {
        return "JsonString(jsonString='$jsonString')"
    }
}


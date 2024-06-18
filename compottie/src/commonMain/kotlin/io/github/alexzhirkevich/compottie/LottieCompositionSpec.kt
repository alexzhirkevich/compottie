package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
import io.github.alexzhirkevich.compottie.dynamic.DynamicComposition
import kotlin.jvm.JvmInline

@Stable
interface LottieCompositionSpec {

    suspend fun load() : LottieComposition

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
) : LottieCompositionSpec  {

    override suspend fun load(): LottieComposition {
        return LottieComposition.parse(jsonString)
    }

    override fun toString(): String {
        return "JsonString(jsonString='$jsonString')"
    }
}


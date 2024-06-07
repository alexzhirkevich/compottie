package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.assets.LottieAssetsManager
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
            "Use overload with lazy loading and assets manager instead",
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
        @OptIn(InternalCompottieApi::class)
        @Stable
        fun JsonString(
            assetsManager: LottieAssetsManager = LottieAssetsManager,
            jsonString: suspend () -> String,
        ): LottieCompositionSpec = LazyJsonString(jsonString, assetsManager)
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
    private val jsonString : suspend () -> String,
    private val assetsManager: LottieAssetsManager,
) : LottieCompositionSpec {

    override suspend fun load(): LottieComposition {
        return LottieComposition.parse(jsonString())
    }

    override fun toString(): String {
        return "LazyJsonString(jsonString='$jsonString')"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? LazyJsonString)?.let {
            it.jsonString == jsonString &&
            it.assetsManager == assetsManager
        } == true
    }

    override fun hashCode(): Int {
        return 31 * jsonString.hashCode() + assetsManager.hashCode()
    }


}



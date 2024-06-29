package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.withContext
import kotlin.jvm.JvmInline

@Stable
interface LottieCompositionSpec {

    /**
     * Key that uniquely identifies composition instance. Equal specs must return equal key
     * */
    val key : String?

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

    override val key: String
        get() = "string_${jsonString.hashCode()}"

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(cacheKey: Any?): LottieComposition {
        return withContext(ioDispatcher()) {
            LottieComposition.getOrCreate(cacheKey) {
                LottieComposition.parse(jsonString)
            }
        }
    }

    override fun toString(): String {
        return "JsonString(jsonString='$jsonString')"
    }
}


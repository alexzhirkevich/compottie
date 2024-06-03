package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import kotlin.jvm.JvmInline

@Stable
sealed interface LottieCompositionSpec {
    suspend fun load(fontFamilyResolver: FontFamily.Resolver) : LottieComposition

    companion object {
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

    override suspend fun load(fontFamilyResolver: FontFamily.Resolver): LottieComposition {
        return LottieComposition.parse(jsonString)
    }

    override fun toString(): String {
        return "JsonString(jsonString='$jsonString')"
    }
}


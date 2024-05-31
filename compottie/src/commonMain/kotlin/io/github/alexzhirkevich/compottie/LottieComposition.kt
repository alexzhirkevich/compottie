package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.compottie.internal.schema.LottieData
import io.github.alexzhirkevich.compottie.internal.schema.LottieJson
import io.github.alexzhirkevich.compottie.internal.schema.durationMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Stable
class LottieComposition internal constructor(
    internal val lottieData: LottieData,
) {
    val duration: Int get() = lottieData.durationMillis

    val frameRate : Int get() = lottieData.frameRate
}

@Composable
@Stable
fun rememberLottieComposition(spec : LottieCompositionSpec) : LottieCompositionResult {

    val result = remember(spec) {
        LottieCompositionResultImpl()
    }

    LaunchedEffect(spec) {
        withContext(Dispatchers.Default) {
            try {
                result.complete(spec.load())
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
    }

    return result
}


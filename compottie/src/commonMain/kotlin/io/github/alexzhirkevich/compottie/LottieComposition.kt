package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.compottie.internal.LottieData
import io.github.alexzhirkevich.compottie.internal.LottieJson
import io.github.alexzhirkevich.compottie.internal.durationMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Stable
class LottieComposition internal constructor(
    internal val lottieData: LottieData,
) {
    val startFrame : Float get()  = lottieData.inPoint

    val endFrame : Float get()  = lottieData.outPoint

    val duration: Float get() = lottieData.durationMillis

    val frameRate: Float get() = lottieData.frameRate

    companion object {
        fun parse(json: String) = LottieComposition(LottieJson.decodeFromString(json))
    }
}

internal fun LottieComposition.marker(name : String?) = lottieData.markers.firstOrNull { it.name == name }

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


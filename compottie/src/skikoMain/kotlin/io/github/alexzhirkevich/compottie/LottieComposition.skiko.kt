package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.alexzhirkevich.network.LottieRepository
import io.github.alexzhirkevich.network.NetworkResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController


@Stable
actual class LottieComposition internal constructor(
    internal val animation: Animation,
    internal val invalidationController: InvalidationController = InvalidationController(),
    internal val lottieData: LottieData
)

internal actual val LottieComposition.fps: Float
    get() = animation.fPS

internal actual val LottieComposition.durationMillis: Float
    get() = animation.duration * 1000

internal actual val LottieComposition.lastFrame : Float
    get() = animation.outPoint

internal actual fun LottieComposition.marker(markerName : String) : Marker? =
    lottieData.markersMap?.get(markerName)



@Composable
@Stable
actual fun rememberLottieComposition(spec : LottieCompositionSpec) : LottieCompositionResult {

    val result = remember(spec) {
        LottieCompositionResultImpl()
    }


    LaunchedEffect(spec){
        when (spec){
            is LottieCompositionSpec.JsonString -> {
                withContext(Dispatchers.Default) {
                    try {
                        val composition = parseFromJsonString(spec.jsonString)
                        result.complete(composition)
                    } catch (c: CancellationException) {
                        throw c
                    } catch (t: Throwable) {
                        result.completeExceptionally(t)
                    }
                }
            }
            is LottieCompositionSpec.Url -> {
                withContext(Dispatchers.Default) {
                    try {
                        val callResult = LottieRepository.getLottieData(spec.url)
                        if (callResult is NetworkResult.Success<*>) {
                            val composition = parseFromJsonString(callResult.data as String)
                            result.complete(composition)
                        } else {
                            result.completeExceptionally((callResult as NetworkResult.Error).exception)
                        }
                    } catch (c: CancellationException) {
                        throw c
                    } catch (t: Throwable) {
                        result.completeExceptionally(t)
                    }
                }
            }
            else -> error("Invalid LottieCompositionSpec: $spec")
        }
    }

    return result
}

private fun parseFromJsonString(jsonString: String) : LottieComposition {
    return LottieComposition(
        animation = Animation.makeFromString(jsonString),
        lottieData = LottieCompositionParser.parse(jsonString)
    )
}

package io.github.alexzhirkevich.compottie

import com.airbnb.lottie.compose.LottieCancellationBehavior

/**
 * Determines how the animation should behave if the calling CoroutineScope is cancelled.
 *
 * @see rememberLottieAnimatable
 */
actual typealias LottieCancellationBehavior = LottieCancellationBehavior
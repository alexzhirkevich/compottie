package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * Holds animation data
 * */
expect class LottieComposition

/**
 * Animation frame per second rate
 * */
expect val LottieComposition.fps : Float

/**
 * Animation duration in milliseconds
 * */
expect val LottieComposition.durationMillis : Float

/**
 * Create and remember lottie composition.
 *
 * @param data Lottie JSON string
 * */
@Composable
expect fun rememberLottieComposition(data : String) : State<LottieComposition?>

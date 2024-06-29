package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable

expect abstract class LottieContext

@Composable
expect fun currentLottieContext() : LottieContext
package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable

actual abstract class LottieContext {

    companion object : LottieContext()
}

@Composable
actual fun currentLottieContext() : LottieContext = LottieContext
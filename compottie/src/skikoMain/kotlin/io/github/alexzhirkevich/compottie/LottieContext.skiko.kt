package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable

public actual abstract class LottieContext {

    internal companion object : LottieContext()
}

@Composable
public  actual fun currentLottieContext() : LottieContext = LottieContext
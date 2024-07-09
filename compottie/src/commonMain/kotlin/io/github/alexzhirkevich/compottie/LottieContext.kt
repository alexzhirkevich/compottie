package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable

public expect abstract class LottieContext

@Composable
public  expect fun currentLottieContext() : LottieContext
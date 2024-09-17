package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher

@InternalCompottieApi
public expect fun Compottie.ioDispatcher() : CoroutineDispatcher
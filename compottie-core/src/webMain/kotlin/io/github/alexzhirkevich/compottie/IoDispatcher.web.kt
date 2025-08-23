package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InternalCompottieApi
public actual fun Compottie.ioDispatcher() : CoroutineDispatcher = Dispatchers.Default
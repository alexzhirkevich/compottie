package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InternalCompottieApi
expect fun ioDispatcher() : CoroutineDispatcher
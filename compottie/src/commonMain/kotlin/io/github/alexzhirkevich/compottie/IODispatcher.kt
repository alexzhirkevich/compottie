package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InternalCompottieApi
public expect fun ioDispatcher() : CoroutineDispatcher
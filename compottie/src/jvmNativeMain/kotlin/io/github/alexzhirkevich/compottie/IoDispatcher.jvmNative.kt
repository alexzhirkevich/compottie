package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@InternalCompottieApi
public actual fun Compottie.ioDispatcher() : CoroutineDispatcher = Dispatchers.IO


package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val Dispatchers.IODispatcher : CoroutineDispatcher
    get() = Dispatchers.Default
package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val Dispatchers.IODispatcher : CoroutineDispatcher
    get() = Dispatchers.IO
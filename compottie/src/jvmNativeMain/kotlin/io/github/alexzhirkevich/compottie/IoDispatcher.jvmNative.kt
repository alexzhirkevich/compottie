package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual fun ioDispatcher() = Dispatchers.IO


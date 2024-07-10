package io.github.alexzhirkevich.compottie

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public actual fun ioDispatcher() : CoroutineDispatcher = Dispatchers.Default
package io.github.alexzhirkevich.compottie.dynamic

import io.github.alexzhirkevich.compottie.internal.AnimationState

typealias PropertyProvider<T> = (source : T, state: AnimationState) -> T

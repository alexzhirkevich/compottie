package io.github.alexzhirkevich.compottie.internal.services

import kotlin.reflect.KClass

internal interface LottieService

internal class LottieServiceLocator(
    private val services: Map<KClass<*>, LottieService>
) {
    inline fun <reified T> get(): T where T : LottieService {
        return services[T::class] as T
    }
}

internal fun LottieServiceLocator(
    vararg services : LottieService
) = LottieServiceLocator(
    services.associateBy { it::class }
)
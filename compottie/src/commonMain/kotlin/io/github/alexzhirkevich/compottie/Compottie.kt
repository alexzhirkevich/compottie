package io.github.alexzhirkevich.compottie

public object Compottie {

    public const val IterateForever : Int = Int.MAX_VALUE

    /**
     * Logger used to inform about various events, errors, unsupported features, etc.
     *
     * Default instance uses stdout / stderr.
     * You can set it to null for production
     * */
    public var logger : LottieLogger? = LottieLogger.Default

    /**
     * Limit gradient shaders cache size.
     *
     * - This value represents the trade-off between memory usage and gradient animations performance.
     *
     * - Most of the time gradients are not animated therefore only 1 instance will be stored
     * - You can set this value at any time (for example when you receive memory usage warning).
     * All running animations will shrink their shader cache size.
     * */
    @ExperimentalCompottieApi
    public var shaderCacheLimit : Int = 10

    /**
     * Limit the number of in-memory cached lottie compositions.
     * */
    @ExperimentalCompottieApi
    public var compositionCacheLimit : Int = 10

    @InternalCompottieApi
    public var context : LottieContext? = null
        internal set
}

package io.github.alexzhirkevich.compottie

private const val TAG = "COMPOTTIE"

interface LottieLogger {

    fun info(message: String)

    fun warn(message: String)

    fun error(message: String, throwable: Throwable)

    /**
     * Uses stdout / stderr.
     * */
    object Default : LottieLogger {
        override fun info(message: String) {
            println("ℹ\uFE0F [$TAG] $message")
        }

        override fun warn(message: String) {
            println("⚠\uFE0F [$TAG] $message")
        }

        override fun error(message: String, throwable: Throwable) {
            println("⛔ [$TAG] $message")
            throwable.printStackTrace()
        }
    }
}
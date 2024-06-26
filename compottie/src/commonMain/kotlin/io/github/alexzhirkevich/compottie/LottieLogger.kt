package io.github.alexzhirkevich.compottie

private const val TAG = "COMPOTTIE"

interface LottieLogger {

    fun info(message: String)

    fun error(message: String, throwable: Throwable)

    /**
     * Uses stdout / stderr.
     * */
    object Default : LottieLogger {
        override fun info(message: String) {
            println("[$TAG] $message")
        }

        override fun error(message: String, throwable: Throwable) {
            println("[$TAG] $message")
            throwable.printStackTrace()
        }
    }
}
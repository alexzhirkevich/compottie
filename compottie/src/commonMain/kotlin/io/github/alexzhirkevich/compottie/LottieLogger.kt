package io.github.alexzhirkevich.compottie

private const val TAG = "COMPOTTIE"

interface LottieLogger {

    fun log(message: String)

    fun error(message: String, throwable: Throwable)

    object Default : LottieLogger {
        override fun log(message: String) {
            println("[$TAG] $message")
        }

        override fun error(message: String, throwable: Throwable) {
            println("[$TAG] $message")
            throwable.printStackTrace()
        }
    }
}
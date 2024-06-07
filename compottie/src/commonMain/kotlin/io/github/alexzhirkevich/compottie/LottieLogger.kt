package io.github.alexzhirkevich.compottie

interface LottieLogger {

    fun log(message: String)

    fun error(message: String, throwable: Throwable)

    object Default : LottieLogger {
        override fun log(message: String) {
            println(message)
        }

        override fun error(message: String, throwable: Throwable) {
            println(message)
            throwable.printStackTrace()
        }
    }
}
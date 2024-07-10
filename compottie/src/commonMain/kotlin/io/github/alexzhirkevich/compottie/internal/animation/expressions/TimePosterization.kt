//package io.github.alexzhirkevich.compottie.internal.animation.expressions
//
//import io.github.alexzhirkevich.compottie.internal.utils.currentTimeMs
//
//internal class TimePosterization {
//
//    private var lastExecutionTime : Long? = null
//
//    private var frameTime : Float? = null
//
//    fun shouldSkipFrame(): Boolean {
//        val frameTime = frameTime
//        val lastExecution = lastExecutionTime
//        if (frameTime == null || lastExecution == null) {
//            lastExecutionTime = currentTimeMs()
//            return false
//        }
//        val time = currentTimeMs()
//        return if (time - lastExecution > frameTime) {
//            lastExecutionTime = time
//            false
//        } else true
//    }
//
//    fun posterizeTime(fps : Float) {
//        frameTime = 1000/fps
//    }
//}
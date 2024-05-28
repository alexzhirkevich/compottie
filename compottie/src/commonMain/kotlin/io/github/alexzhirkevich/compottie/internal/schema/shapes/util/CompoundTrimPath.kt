//package io.github.alexzhirkevich.compottie.internal.schema.shapes.util
//
//import androidx.compose.ui.graphics.Path
//import io.github.alexzhirkevich.compottie.internal.schema.shapes.TrimPath
//
//internal class CompoundTrimPath {
//    private val contents: MutableList<TrimPath> = mutableListOf()
//
//    fun addTrimPath(trimPath: TrimPath) {
//        contents.add(trimPath)
//    }
//
//    fun apply(path: Path, time : Int) {
//        for (i in contents.indices.reversed()) {
//            Utils.applyTrimPathIfNeeded(path = path, trimPath = contents[i], time = time)
//        }
//    }
//}

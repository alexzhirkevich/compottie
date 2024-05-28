//package io.github.alexzhirkevich.compottie.standalone.schema.layers
//
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.graphics.Matrix
//import androidx.compose.ui.util.fastForEachReversed
//import io.github.alexzhirkevich.compottie.standalone.schema.DrawableContent
//
//abstract class BaseLayer : VisualLayer, DrawableContent {
//
//    private val boundsMatrix = Matrix()
//
//    private var parentLayers: List<BaseLayer> = emptyList()
//
//    private var parentLayer: BaseLayer? = null
//
//    override fun getBounds(outBounds: Rect, parentMatrix: Matrix, applyParents: Boolean) {
//        val rect = Rect(0f, 0f, 0f, 0f)
//
//        buildParentLayerListIfNeeded()
//
//        boundsMatrix.setFrom(parentMatrix)
//
//        if (applyParents) {
//            if (parentLayers.isNotEmpty()) {
//                parentLayers.fastForEachReversed {
//                    boundsMatrix *= it.transform.getMatrix()
//                }
//                for (i in parentLayers.indices.reversed()) {
//                    boundsMatrix.preConcat(parentLayers[i].transform.getMatrix())
//                }
//            } else if (parentLayer != null) {
//                boundsMatrix.preConcat(parentLayer.transform.getMatrix())
//            }
//        }
//
//        boundsMatrix.preConcat(transform.getMatrix())
//    }
//
//    private fun buildParentLayerListIfNeeded() {
//        val list = mutableListOf<BaseLayer>()
//
//        parentLayers = mutableListOf()
//        val layer = parentLayer
//        while (layer != null) {
//            list.add(layer)
//            parentLayers = list
//        }
//    }
//}
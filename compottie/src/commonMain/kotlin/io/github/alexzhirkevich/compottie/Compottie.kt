package io.github.alexzhirkevich.compottie

import io.github.alexzhirkevich.compottie.internal.Animation
import io.github.alexzhirkevich.compottie.internal.LottieJson

object Compottie {

    const val IterateForever = Int.MAX_VALUE

    /**
     * Logger used to inform about various events, errors, unsupported features, etc.
     *
     * Default instance uses stdout / stderr.
     * You can set it to null for production
     * */
    var logger : LottieLogger? = LottieLogger.Default

    /**
     * Limit gradient shaders cache size.
     *
     * - This value represents the trade-off between memory usage and gradient animations performance.
     *
     * - Most of the time gradients are not animated therefore only 1 instance will be stored
     *
     * - Single animated gradient caches ~10 shaders per refresh rate (so assume on 60hz devices
     * each animated gradient will try to cache ~600 shader instances)
     *
     * - By default each animated gradient instance keeps up to 1000 shader instances.
     * If you have many composed animations that have many animated gradients,
     * you can experience high memory usage. In that case you can lower this value.
     *
     * - You can set this value at any time (for example when you receive memory usage warning).
     * All running animations will shrink their shader cache size.
     * */
    @ExperimentalCompottieApi
    var shaderCacheLimit : Int = 1000

    /**
     * Limit the number of in-memory cached lottie compositions.
     * */
    @ExperimentalCompottieApi
    var compositionCacheLimit : Int = 15


    /**
     * Warmup JSON parser. The first animation parsing will be much faster
     * */
    @ExperimentalCompottieApi
    fun warmup() {
        LottieJson.decodeFromString<Animation>(warmupAnim)
    }
}


private val warmupAnim by lazy {
    """
        {"v":"5.7.1","fr":60,"ip":0,"op":181,"w":500,"h":500,"nm":"Comp 1","ddd":0,"fonts":{"list":[{"fPath":"","fFamily":"","fStyle":"","fName":"","origin":3}]},"assets":[{"id":"comp_0","layers":[]},{"id":"blep","h":512,"w":512,"u":"/images/","p":"blep.png","e":1}],"layers":[{"ddd":0,"ind":1,"ty":0,"nm":"Pre-comp 1","refId":"comp_0","sr":1,"ks":{"o":{"a":0,"k":100,"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[250,268,0],"ix":2},"a":{"a":0,"k":[1000,1000,0],"ix":1},"s":{"a":0,"k":[27,27,100],"ix":6}},"ao":0,"w":2000,"h":2000,"ip":0,"op":600,"st":0,"bm":0},{"ddd":0,"ind":1,"ty":4,"nm":"ÃÂÃÂÃÂºÃÂ° 1.1 Outlines 2","cl":"1","parent":3,"sr":1,"ks":{"o":{"a":0,"k":100,"ix":11},"r":{"a":0,"k":0,"ix":10},"p":{"a":0,"k":[195.262,126.787,0],"ix":2},"a":{"a":0,"k":[195.262,126.787,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6}},"ao":0,"hasMask":true,"masksProperties":[{"mode":"a","o":{"a":0,"k":100},"inv":false,"x":{"a":0,"k":0},"pt":{"a":0,"k":{"c":true,"v":[[247.109,125.719],[66.109,306.719],[424.109,389.719]],"i":[[0,0],[0,0],[0,0]],"o":[[0,0],[0,0],[0,0]]}}}],"shapes":[{"ty":"gr","it":[{"ind":0,"ty":"sh","ix":1,"ks":{"a":0,"k":{"i":[[0.885,0.104],[-0.781,0.233]],"o":[[-0.629,-0.074],[0.782,-0.232]],"v":[[-0.258,-2.695],[0.105,2.535]],"c":true},"ix":2},"nm":"Path 1","hd":false},{"ty":"tm","bm":0,"hd":false,"mn":"ADBE Vector Filter - Trim","nm":"Trim Paths 1","ix":2,"e":{"a":1,"k":[{"o":{"x":0.333,"y":0},"i":{"x":0,"y":1},"s":[0],"t":5},{"s":[100],"t":22}],"ix":2},"o":{"a":0,"k":0,"ix":3},"s":{"a":1,"k":[{"o":{"x":0.463,"y":0},"i":{"x":0.853,"y":1},"s":[0],"t":22},{"s":[100],"t":36}],"ix":1},"m":1},{"ty":"mm","mm":4,"nm":"Merge Paths 1","hd":false},{"ty":"fl","c":{"a":0,"k":[1,1,1,1],"ix":4},"o":{"a":0,"k":100,"ix":5},"r":1,"bm":0,"nm":"Fill 1","hd":false},{"ty":"tr","p":{"a":0,"k":[192.227,107.118],"ix":2},"a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"r":{"a":0,"k":0,"ix":6},"o":{"a":0,"k":100,"ix":7},"sk":{"a":0,"k":0,"ix":4},"sa":{"a":0,"k":0,"ix":5},"nm":"Transform"}],"nm":"Group 1","np":4,"cix":2,"bm":0,"ix":1,"hd":false}],"ip":0,"op":600,"st":0,"bm":0},{"nm":"Text Layer","ty":5,"sr":1,"ks":{"p":{"k":[5,80],"a":0}},"ip":0,"op":120,"st":0,"t":{"a":[],"d":{"k":[{"s":{"f":"Ubuntu Light Italic","fc":[0,0,0],"s":100,"t":"Hello","j":0,"sc":[0,0,0],"sw":0},"t":0}]},"m":{"a":{"k":[0,0],"a":0}},"p":{}}},{"ddd":0,"ty":2,"sr":1,"ks":{"a":{"k":[0,0],"a":0},"p":{"k":[0,0],"a":0},"s":{"k":[100,100],"a":0},"r":{"k":0,"a":0},"o":{"k":100,"a":0},"sk":{"k":0,"a":0},"sa":{"k":0,"a":0}},"ao":0,"ip":0,"op":60,"st":0,"bm":0,"ind":0,"refId":"blep"}],"markers":[]}
    """.trimIndent()
}
package js

import io.github.alexzhirkevich.skriptie.ecmascript.invoke
import io.github.alexzhirkevich.skriptie.javascript.JS

internal fun String.runJs() : Any {
    return JS().invoke(this)
}
//
//internal fun String.assertSimpleExprEquals(expected : Any) {
//    "var $ret=$this".assertSimpleExprReturns(expected)
//}
//
//internal fun String.assertSimpleExprReturns(expected : Any) {
//    assertEquals(expected, runJs())
//}
//
//internal fun String.assertSimpleExprEquals(expected : Double) {
//    "var $ret=$this".assertSimpleExprReturns(expected)
//}
//
//internal fun String.assertSimpleExprReturns(expected : Double) {
//    assertEquals(expected, runJs() as Double, 0.00001)
//}

package js

import io.github.alexzhirkevich.skriptie.invoke
import io.github.alexzhirkevich.skriptie.javascript.JS
import kotlin.test.assertEquals

internal fun Any?.assertEqualsTo(other : Any?) = assertEquals(other,this)
internal fun Any?.assertEqualsTo(other : Double, tolerance: Double = 0.0001) =
    assertEquals(other,this as Double, tolerance)

internal fun String.runJs() : Any? {
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

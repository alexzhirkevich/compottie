package expressions.global

import expressions.assertSimpleExprEquals
import kotlin.test.Test

class BooleanExpressionsTest {

    @Test
    fun and() {
        "true && true".assertSimpleExprEquals(true)
        "true && true && true".assertSimpleExprEquals(true)

        "false && false".assertSimpleExprEquals(false)
        "true && false".assertSimpleExprEquals(false)
        "false && true".assertSimpleExprEquals(false)

        "true && true && false".assertSimpleExprEquals(false)
        "false && true && true".assertSimpleExprEquals(false)
    }

    @Test
    fun or() {
        "true || true".assertSimpleExprEquals(true)
        "true || true || true".assertSimpleExprEquals(true)

        "true || false".assertSimpleExprEquals(true)
        "false || true".assertSimpleExprEquals(true)
        "false || false".assertSimpleExprEquals(false)

        "true || true || false".assertSimpleExprEquals(true)
        "false || true || true".assertSimpleExprEquals(true)
        "false || false || true".assertSimpleExprEquals(true)
        "false || false || false".assertSimpleExprEquals(false)
    }

    @Test
    fun and_or_order() {
        "true && true || false".assertSimpleExprEquals(true)
        "false || true && true".assertSimpleExprEquals(true)

        "(false && true) || (true && true)".assertSimpleExprEquals(true)
        "false && true || true && true".assertSimpleExprEquals(true)
        "(false && true || true) && true".assertSimpleExprEquals(true)
//
        "true || false && false".assertSimpleExprEquals(true)
        "(true || false) && false".assertSimpleExprEquals(false)
    }

    @Test
    fun with_different_source() {
        "false || 1 == 1".assertSimpleExprEquals(true)
        "true && 1 == 2".assertSimpleExprEquals(false)

        "(1 == 2) || false || (1+1) == 2".assertSimpleExprEquals(true)
        "1 == 2 || false || (1+1) == 2".assertSimpleExprEquals(true)
        "1 == 1 && 2 == 2".assertSimpleExprEquals(true)

        "1 == 2 && 2 == 1 || 2 * 2 == 4".assertSimpleExprEquals(true)
    }
}
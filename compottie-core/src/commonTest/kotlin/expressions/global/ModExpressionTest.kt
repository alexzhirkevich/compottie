package expressions.global

import expressions.assertExprEquals
import kotlin.test.Test


class ModExpressionTest {

    @Test
    fun mod_num(){
        "mod(25,4)".assertExprEquals(1f)
        "mod(25.1,4)".assertExprEquals(1.1f)
    }
}


package expressions.global

import expressions.assertExprValueEquals
import kotlin.test.Test


internal class ModExpressionTest {

    @Test
    fun mod_num(){
        "mod(25,4)".assertExprValueEquals(1f)
        "mod(25.1,4)".assertExprValueEquals(1.1f)
    }
}


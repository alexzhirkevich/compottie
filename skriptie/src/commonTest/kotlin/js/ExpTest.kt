package js

import kotlin.test.Test

class ExpTest {

    @Test
    fun expOperator(){
        "4 ** 2".eval().assertEqualsTo(16.0)
    }

    @Test
    fun rightAssociativity(){
        // Same as 4 ** (3 ** 2); evaluates to 262144
        "4 ** 3 ** 2".eval().assertEqualsTo(262144.0)
    }
}
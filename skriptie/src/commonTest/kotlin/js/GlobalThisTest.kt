package js

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalThisTest {

    @Test
    fun recursive() {
        assertTrue { "globalThis == globalThis.globalThis".eval() as Boolean }
    }
}
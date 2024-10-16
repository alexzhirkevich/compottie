package js

import io.github.alexzhirkevich.skriptie.javascript.JSRuntime
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalThisTest {

    @Test
    fun recursive() {
        assertTrue { "globalThis == globalThis.globalThis".eval() as Boolean }
    }

    @Test
    fun instance() {
        val runtime = JSRuntime().apply { set("runtime", this) }
        assertTrue { "runtime == globalThis".eval(runtime) as Boolean }
    }
}
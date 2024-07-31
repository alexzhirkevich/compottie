package js

import kotlin.test.Test

class ConditionTest {

    @Test
    fun if_with_else() {
        "var x = 1; if (true) { x = x+1 }; x".eval().assertEqualsTo(2L)
        "var x = 1; if (true) x+=1; x".eval().assertEqualsTo(2L)
        "var x = 1; if (1==1) { x = x+1 }; x".eval().assertEqualsTo(2L)
        "var x = 1; if (1==1) x +=1; x".eval().assertEqualsTo(2L)
        "var x = 1; if (true) { x = x+1;x = x+1 }; x".eval().assertEqualsTo(3L)
        "var x = 1; if (true) { x = x+1\nx +=1 }; x".eval().assertEqualsTo(3L)

        "var x = 1; if (false) { x = x } else { x = x+1 }; x"
            .eval().assertEqualsTo(2L)

        "var x = 1.0; if (false) x +=1; x"
            .eval().assertEqualsTo(1.0)

        "var x = 1; if (1 != 1) x = 0 else { x = x+1;x = x+1 }; x"
            .eval().assertEqualsTo(3L)

        "var x = 1; if (!(1 == 1)) x = 0 else { x +=1;x = x+1 }; x"
            .eval().assertEqualsTo(3L)

        "var x = 0; if (true) { if (true) { x +=1 } }; x"
            .eval().assertEqualsTo(1L)

        "var x = 0; if (true) { if (false) { x = 0 } else { x += 1 } }; x"
            .eval().assertEqualsTo(1L)

        "var x = 0; if (true) if (false) x = 0 else x += 1; x "
            .eval().assertEqualsTo(1L)
    }
}
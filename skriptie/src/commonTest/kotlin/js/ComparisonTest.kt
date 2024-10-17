package js

import kotlin.test.Test

class ComparisonTest {

    @Test
    fun number_string(){
        "'1' > 2".eval().assertEqualsTo(false)
        "'1' == 1".eval().assertEqualsTo(true)
        "'1' === 1".eval().assertEqualsTo(false)

        "'test' == 0".eval().assertEqualsTo(false)
        "'test' == NaN".eval().assertEqualsTo(false)

        "'00100' < '1'".eval().assertEqualsTo(true)
        "'00100' < 1".eval().assertEqualsTo(false)

        " +'2' > +'10'".eval().assertEqualsTo(false)
        "'2' > '10'".eval().assertEqualsTo(true)
    }
}
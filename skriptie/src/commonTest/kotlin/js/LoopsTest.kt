package js

import kotlin.test.Test

class LoopsTest {

    @Test
    fun whileLoop() {
        """
            var x = 0
            while(x != 3) {
                x += 1
            }
            x
        """.trimIndent().eval().assertEqualsTo(3L)

        """
            var x = 0
            while(x < 3)
                x += 1
            x
        """.trimIndent().eval().assertEqualsTo(3L)
    }

    @Test
    fun doWhileLoop() {
        """
            var x = 0
            do {
                x+=1
            } while(x != 3)
            x
        """.trimIndent().eval().assertEqualsTo(3L)
    }

    @Test
    fun forLoop(){
        """
            var x = 0
            for(let i = 0; i<3;i++){
                x+=1
            }
            x
        """.trimIndent().eval().assertEqualsTo(3L)

        """
            let i = 0
            for(i = 0; i<3;i++){
                
            }
            i
        """.trimIndent().eval().assertEqualsTo(3L)

        """
            let i = 0
            for(i = 0; i<3;){
                i++
            }
            i
        """.trimIndent().eval().assertEqualsTo(3L)

        """
            let i = 0
            for(; i<3;){
                i++
            }
            i
        """.trimIndent().eval().assertEqualsTo(3L)

        """
            let i = 0
            for(;;){
                i++
                if (i >= 3) 
                    break
            }
            i
        """.trimIndent().eval().assertEqualsTo(3L)
    }

    @Test
    fun early_return(){
        """
            var x = 0
            for(let i = 0; i<3;i++){
                break
                x+=1
            }
            x
        """.trimIndent().eval().assertEqualsTo(0L)

        """
            var x = 0
            for(let i = 0; i<3;i++){
                if (i % 2 == 1)
                    continue
                x+=1
            }
            x
        """.trimIndent().eval().assertEqualsTo(2L)

        """
            var i = 0
            var x = 0
            while(x < 3) {
                i++
                if (i == 1)
                    break
                x += 1
            }
            i
        """.trimIndent().eval().assertEqualsTo(1L)

        """
            var i = 0
            var x = 0
             do {
                i++
                if (i % 2 == 1)
                    continue
                x += 1
            } while(x < 3)
            i
        """.trimIndent().eval().assertEqualsTo(6L)

        """
            var i = 0
            var x = 0
            while(x < 3) {
                i++
                if (i == 1)
                    break
                x += 1
            }
            i
        """.trimIndent().eval().assertEqualsTo(1L)

        """
            var i = 0
            var x = 0
            do {
                i++
                if (i % 2 == 1)
                    continue
                x += 1
            } while(x < 3)
            i
        """.trimIndent().eval().assertEqualsTo(6L)
    }
}
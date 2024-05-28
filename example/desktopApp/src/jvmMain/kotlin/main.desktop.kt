@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

import androidx.compose.ui.window.singleWindowApplication

fun main() {
    singleWindowApplication {
        App()
    }
}


private val testLottie = """
{
  "nm": "Comp 1",
  "ddd": 0,
  "h": 800,
  "w": 800,
  "meta": {
    "g": "@lottiefiles/toolkit-js 0.33.2"
  },
   "v": "5.5.9",
  "fr": 24,
  "op": 84,
  "ip": 0,
  "layers" : [
    {
        "ty" : 4,
        "nm" : "shape layer"
    }
  ]
}
""".trimIndent()
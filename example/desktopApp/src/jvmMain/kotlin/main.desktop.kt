@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    singleWindowApplication(
        title = "Compottie Example",
        state = WindowState(
            size = DpSize(1295.dp, 500.dp)
        )
    ) {
        App()
    }
}



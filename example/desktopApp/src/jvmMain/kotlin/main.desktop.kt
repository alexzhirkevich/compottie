import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    singleWindowApplication(
        title = "Compottie 2.0 Example",
        state = WindowState(
            size = DpSize(800.dp, 600.dp)
        )
    ) {
        App()
    }
}


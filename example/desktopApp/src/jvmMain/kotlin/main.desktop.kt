import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    singleWindowApplication(
        title = "Compottie 2.0 Example",
        state = WindowState(
            size = DpSize(1024.dp, 720.dp)
        )
    ) {
        App()
    }
}


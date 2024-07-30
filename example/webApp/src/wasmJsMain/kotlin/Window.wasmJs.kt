import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun CompatComposeWindow(
    title : String?,
    content : @Composable () -> Unit
) {
    CanvasBasedWindow(
        title = title,
        content = content
    )
}
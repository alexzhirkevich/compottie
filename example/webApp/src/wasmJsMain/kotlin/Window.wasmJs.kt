
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun CompatComposeWindow(
    containerId : String?,
    content : @Composable () -> Unit
) {
    ComposeViewport(
        viewportContainerId = containerId,
        content = content
    )
}
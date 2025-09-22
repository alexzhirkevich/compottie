
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun CompatComposeWindow(
    containerId : String?,
    content : @Composable () -> Unit
){
    onWasmReady {
        ComposeViewport(
            viewportContainerId = containerId,
            content = content
        )
    }
}
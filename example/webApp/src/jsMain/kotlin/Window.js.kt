import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
internalactual fun CompatComposeWindow(
    title : String?,
    content : @Composable () -> Unit
){
    onWasmReady {
        CanvasBasedWindow(
            title = title,
            content = content
        )
    }
}
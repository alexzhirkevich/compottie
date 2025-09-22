import androidx.compose.runtime.Composable


internal expect fun CompatComposeWindow(
    containerId : String? = null,
    content : @Composable () -> Unit
)
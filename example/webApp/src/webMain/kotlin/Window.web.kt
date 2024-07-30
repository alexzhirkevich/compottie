import androidx.compose.runtime.Composable


internal expect fun CompatComposeWindow(
    title : String? = null,
    content : @Composable () -> Unit
)
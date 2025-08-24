import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@OptIn(ExperimentalComposeUiApi::class)
public fun MainViewController() : UIViewController  = ComposeUIViewController(
    configure = {
//        parallelRendering = false
    }
) {

    App()
}
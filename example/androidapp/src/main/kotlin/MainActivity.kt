package io.github.alexzhirkevich.compottie.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import App
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext as Application

        lifecycleScope.launch {
            val bytes = renderComposeScene(256,256){
                Box(Modifier.fillMaxSize().background(Color.Red))
            }

            val bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
            setContent {
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
            }
        }
    }
}
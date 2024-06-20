package io.github.alexzhirkevich.compottie.example.android

import App
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
          StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                           .detectAll()
                           .penaltyLog()
                           .penaltyDeath()
                           .build());
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
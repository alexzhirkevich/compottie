package io.github.alexzhirkevich.compottie

import android.content.Context
import androidx.startup.Initializer

public class CompottieInitializer : Initializer<Compottie> {

    @OptIn(InternalCompottieApi::class)
    override fun create(context: Context): Compottie {
        Compottie.context = context
        return Compottie
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}
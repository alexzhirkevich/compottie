package io.github.alexzhirkevich.compottie

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.github.alexzhirkevich.compottie.internal.LottieData
import io.github.alexzhirkevich.compottie.internal.LottieJson

@InternalCompottieApi
var L.context : Context
    get() = checkNotNull(AndroidContextProvider.ANDROID_CONTEXT) {
        "Android context failed to initialize. Probably applicationId is not set in the build.gradle"
    }
    internal set(value) {
        AndroidContextProvider.ANDROID_CONTEXT = value.applicationContext
    }


//https://andretietz.com/2017/09/06/autoinitialise-android-library/
internal class AndroidContextProvider : ContentProvider() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var ANDROID_CONTEXT: Context? = null
    }

    override fun onCreate(): Boolean {
        ANDROID_CONTEXT = context?.applicationContext
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
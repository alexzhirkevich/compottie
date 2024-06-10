package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import okio.Buffer
import okio.FileSystem


@Composable
fun rememberDiskCacheStrategy() : LottieCacheStrategy {
    return remember {
        DiskCacheStrategy()
    }


}

@Stable
class DiskCacheStrategy : LottieCacheStrategy {

    override suspend fun save(url: String, byteArray: ByteArray) {
        val key = Buffer().write(url.encodeToByteArray()).md5().hex()

        FileSystem.SYSTEM_TEMPORARY_DIRECTORY
    }

    override suspend fun load(url: String): ByteArray? {
        return null
    }

    override fun equals(other: Any?): Boolean {
        return true
    }

    override fun hashCode(): Int {
        return 1
    }
}

//internal expect fun FileSystem.Companion.System : FileSystem
package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import okio.Buffer
import okio.use

@Stable
class DiskCacheStrategy(
    private val diskCache: DiskCache = SharedDiskCache
) : LottieCacheStrategy {

    override suspend fun save(url: String, bytes: ByteArray) {
        val editor = diskCache.openEditor(key(url)) ?: return
        try {
            diskCache.fileSystem.write(editor.data) {
                write(bytes)
            }
            editor.commit()
        } catch (t: Throwable) {
            editor.abort()
        }
    }

    override suspend fun load(url: String): ByteArray? {
        val snapshot = diskCache.openSnapshot(key(url)) ?: return null

        snapshot.use {
            val bytes = diskCache.fileSystem.read(it.data) {
                readByteArray()
            }

            return bytes
        }
    }

    override fun equals(other: Any?): Boolean {
        return diskCache == (other as? DiskCacheStrategy)?.diskCache
    }

    override fun hashCode(): Int {
        return 31 * diskCache.hashCode()
    }

    private fun key(url: String) = Buffer().write(url.encodeToByteArray()).md5().hex()
}


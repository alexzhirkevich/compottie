package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import okio.Buffer
import okio.FileSystem
import okio.use

@Stable
class DiskCacheStrategy(
    private val diskCache: DiskCache = SharedDiskCache
) : LottieCacheStrategy {

    override suspend fun save(url: String, byteArray: ByteArray) {
        try {
            val editor = diskCache.openEditor(key(url)) ?: return
            try {
                diskCache.fileSystem.write(editor.data) {
                    write(byteArray)
                }
            } finally {
                editor.commit()
            }
        } catch (_: Throwable) {
        }
    }

    override suspend fun load(url: String): ByteArray? {
        try {
            val snapshot = diskCache.openSnapshot(key(url)) ?: return null

            snapshot.use {
                val bytes = diskCache.fileSystem.read(it.data) {
                    readByteArray()
                }

                return bytes
            }
        } catch (t: Throwable) {
            return null
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


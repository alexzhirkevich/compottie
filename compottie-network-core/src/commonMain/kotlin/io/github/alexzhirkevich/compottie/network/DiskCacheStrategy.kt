package io.github.alexzhirkevich.compottie.network

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import okio.ByteString.Companion.encodeUtf8
import okio.Path
import okio.use

@Stable
public class DiskCacheStrategy(
    private val diskCache: DiskCache = SharedDiskCache
) : LottieCacheStrategy {

    override fun path(url: String): Path? {
        return try {
            diskCache.openSnapshot(key(url)).use { it?.data }
        } catch (t: Throwable) {
            null
        }
    }

    override suspend fun save(url: String, bytes: ByteArray): Path? {
        val editor = diskCache.openEditor(key(url)) ?: return null

        return try {
            diskCache.fileSystem.write(editor.data) {
                write(bytes)
            }
            editor.commitAndOpenSnapshot().use {
                it?.data
            }
        } catch (t: Throwable) {
            editor.abort()
            null
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

    override suspend fun clear() {
        diskCache.clear()
    }

    override fun equals(other: Any?): Boolean {
        return diskCache == (other as? DiskCacheStrategy)?.diskCache
    }

    override fun hashCode(): Int {
        return 31 * diskCache.hashCode()
    }

    private fun key(url: String) = url.encodeUtf8().sha256().hex()

    public companion object {

        @InternalCompottieApi
        public val Instance: DiskCacheStrategy by lazy {
            DiskCacheStrategy()
        }
    }
}


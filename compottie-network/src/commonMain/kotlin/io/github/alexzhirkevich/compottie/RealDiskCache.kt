package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineDispatcher
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path

@Stable
internal class RealDiskCache(
    override val maxSize: Long,
    override val directory: Path,
    override val fileSystem: FileSystem,
    cleanupDispatcher: CoroutineDispatcher,
) : DiskCache {

    private val cache = DiskLruCache(
        fileSystem = fileSystem,
        directory = directory,
        cleanupDispatcher = cleanupDispatcher,
        maxSize = maxSize,
        appVersion = 2,
        valueCount = 2,
    )

    override val size get() = cache.size()

    override fun openSnapshot(key: String): DiskCache.Snapshot? {
        return cache[key.hash()]?.let(RealDiskCache::RealSnapshot)
    }

    override fun openEditor(key: String): DiskCache.Editor? {
        return cache.edit(key.hash())?.let(RealDiskCache::RealEditor)
    }

    override fun remove(key: String): Boolean {
        return cache.remove(key.hash())
    }

    override fun clear() {
        cache.evictAll()
    }

    @OptIn(InternalCompottieApi::class)
    override fun shutdown() {
        cache.closeQuietly()
    }

    private fun String.hash() = encodeUtf8().sha256().hex()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RealDiskCache

        if (maxSize != other.maxSize) return false
        if (directory != other.directory) return false
        if (fileSystem != other.fileSystem) return false
        if (cache != other.cache) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maxSize.hashCode()
        result = 31 * result + directory.hashCode()
        result = 31 * result + fileSystem.hashCode()
        result = 31 * result + cache.hashCode()
        return result
    }

    private class RealSnapshot(private val snapshot: DiskLruCache.Snapshot) : DiskCache.Snapshot {

        override val metadata get() = snapshot.file(ENTRY_METADATA)
        override val data get() = snapshot.file(ENTRY_DATA)

        override fun close() = snapshot.close()
        override fun closeAndOpenEditor() = snapshot.closeAndEdit()?.let(RealDiskCache::RealEditor)
    }

    private class RealEditor(private val editor: DiskLruCache.Editor) : DiskCache.Editor {

        override val metadata get() = editor.file(ENTRY_METADATA)
        override val data get() = editor.file(ENTRY_DATA)

        override fun commit() = editor.commit()
        override fun commitAndOpenSnapshot() = editor.commitAndGet()?.let(RealDiskCache::RealSnapshot)
        override fun abort() = editor.abort()
    }

    companion object {
        private const val ENTRY_METADATA = 0
        private const val ENTRY_DATA = 1
    }
}
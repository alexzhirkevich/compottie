package io.github.alexzhirkevich.compottie.network

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.compottie.InternalCompottieApi
import io.github.alexzhirkevich.compottie.defaultFileSystem
import io.github.alexzhirkevich.compottie.ioDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import okio.Closeable
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.js.JsName

/**
 * An LRU cache of files.
 */
@Stable
public interface DiskCache {

    /** The current size of the cache in bytes. */
    public val size: Long

    /** The maximum size of the cache in bytes. */
    public val maxSize: Long

    /** The directory that contains the cache's files. */
    public val directory: Path

    /** The file system that contains the cache's files. */
    public val fileSystem: FileSystem

    /**
     * Read the entry associated with [key].
     *
     * IMPORTANT: **You must** call either [Snapshot.close] or [Snapshot.closeAndOpenEditor] when
     * finished reading the snapshot. An open snapshot prevents opening a new [Editor] or deleting
     * the entry on disk.
     */
    public fun openSnapshot(key: String): Snapshot?

    /**
     * Write to the entry associated with [key].
     *
     * IMPORTANT: **You must** call one of [Editor.commit], [Editor.commitAndOpenSnapshot], or
     * [Editor.abort] to complete the edit. An open editor prevents opening a new [Snapshot],
     * opening a new [Editor], or deleting the entry on disk.
     */
    public fun openEditor(key: String): Editor?

    /**
     * Delete the entry referenced by [key].
     *
     * @return 'true' if [key] was removed successfully. Else, return 'false'.
     */
    public fun remove(key: String): Boolean

    /**
     * Delete all entries in the disk cache.
     */
    public fun clear()

    /**
     * Close any open snapshots, abort all in-progress edits, and close any open system resources.
     */
    public fun shutdown()

    /**
     * A snapshot of the values for an entry.
     *
     * IMPORTANT: You must **only read** [metadata] or [data]. Mutating either file can corrupt the
     * disk cache. To modify the contents of those files, use [openEditor].
     */
    public interface Snapshot : Closeable {

        /** Get the metadata file path for this entry. */
        public val metadata: Path

        /** Get the data file path for this entry. */
        public val data: Path

        /** Close the snapshot to allow editing. */
        override fun close()
    }

    /**
     * Edits the values for an entry.
     *
     * Calling [metadata] or [data] marks that file as dirty so it will be persisted to disk
     * if this editor is committed.
     *
     * IMPORTANT: You must **only read or modify the contents** of [metadata] or [data].
     * Renaming, locking, or other mutating file operations can corrupt the disk cache.
     */
    public interface Editor {

        /** Get the metadata file path for this entry. */
        public val metadata: Path

        /** Get the data file path for this entry. */
        public val data: Path

        /** Commit the edit so the changes are visible to readers. */
        public fun commit()

        /** Commit the write and call [openSnapshot] for this entry atomically. */
        public fun commitAndOpenSnapshot(): Snapshot?

        /** Abort the edit. Any written data will be discarded. */
        public fun abort()
    }

}

internal val SharedDiskCache by lazy {
    DiskCache()
}

@OptIn(InternalCompottieApi::class)
@JsName("LottieDiskCache")
@Stable
public fun DiskCache(
    directory: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("compottie_disc_cache".toPath()),
    fileSystem : FileSystem = defaultFileSystem(),
    maxSizeBytes : Long = MB_250,
    cleanupDispatcher : CoroutineDispatcher = ioDispatcher()
) : DiskCache = RealDiskCache(
    maxSize = maxSizeBytes,
    directory = directory,
    fileSystem = fileSystem,
    cleanupDispatcher = cleanupDispatcher
)

private const val MB_250 = 250L * 1024 * 1024
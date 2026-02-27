package com.jetpackduba.gitnuro.git.workspace

import com.jetpackduba.gitnuro.extensions.flatListOf
import com.jetpackduba.gitnuro.git.EntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Status
import javax.inject.Inject

class GetStagedUseCase @Inject constructor() {
    suspend operator fun invoke(status: Status) =
        withContext(Dispatchers.IO) {
            val added = status.added.map {
                StatusEntry(it, StatusType.ADDED, entryType = EntryType.STAGED)
            }
            val modified = status.changed.map {
                StatusEntry(it, StatusType.MODIFIED, entryType = EntryType.STAGED)
            }
            val removed = status.removed.map {
                StatusEntry(it, StatusType.REMOVED, entryType = EntryType.STAGED)
            }

            return@withContext flatListOf(
                added,
                modified,
                removed,
            ).sortedBy { it.filePath }
        }
}
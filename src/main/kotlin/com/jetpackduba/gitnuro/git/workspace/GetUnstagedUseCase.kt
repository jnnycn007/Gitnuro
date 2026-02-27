package com.jetpackduba.gitnuro.git.workspace

import com.jetpackduba.gitnuro.extensions.flatListOf
import com.jetpackduba.gitnuro.git.EntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Status
import javax.inject.Inject

class GetUnstagedUseCase @Inject constructor() {
    suspend operator fun invoke(status: Status) = withContext(Dispatchers.IO) {
        val untracked = status.untracked.map {
            StatusEntry(it, StatusType.ADDED, entryType = EntryType.UNSTAGED)
        }
        val modified = status.modified.map {
            StatusEntry(it, StatusType.MODIFIED, entryType = EntryType.UNSTAGED)
        }
        val missing = status.missing.map {
            StatusEntry(it, StatusType.REMOVED, entryType = EntryType.UNSTAGED)
        }
        val conflicting = status.conflicting.map {
            StatusEntry(it, StatusType.CONFLICTING, entryType = EntryType.UNSTAGED)
        }

        return@withContext flatListOf(
            untracked,
            modified,
            missing,
            conflicting,
        ).sortedBy { it.filePath }
    }
}
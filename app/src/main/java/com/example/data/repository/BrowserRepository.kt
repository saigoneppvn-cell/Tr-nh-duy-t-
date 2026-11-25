package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.data.local.HistoryEntity
import com.example.data.local.SavedTabEntity
import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val db: AppDatabase) {

    val bookmarks: Flow<List<BookmarkEntity>> = db.bookmarkDao().observeAll()
    val history: Flow<List<HistoryEntity>> = db.historyDao().observeRecent()
    val downloads: Flow<List<DownloadEntity>> = db.downloadDao().observeAll()

    fun observeIsBookmarked(url: String): Flow<Boolean> = db.bookmarkDao().observeIsBookmarked(url)

    suspend fun toggleBookmark(title: String, url: String): Boolean {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return false
        val existing = db.bookmarkDao().findByUrl(cleanUrl)
        return if (existing != null) {
            db.bookmarkDao().deleteByUrl(cleanUrl)
            false
        } else {
            db.bookmarkDao().insert(
                BookmarkEntity(
                    title = title.ifBlank { cleanUrl },
                    url = cleanUrl
                )
            )
            true
        }
    }

    suspend fun addBookmark(title: String, url: String) {
        if (url.isBlank()) return
        db.bookmarkDao().insert(BookmarkEntity(title = title.ifBlank { url }, url = url.trim()))
    }

    suspend fun removeBookmark(id: Long) = db.bookmarkDao().deleteById(id)

    suspend fun clearBookmarks() = db.bookmarkDao().clearAll()

    suspend fun recordHistory(title: String, url: String) {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("data:")) return
        db.historyDao().insert(
            HistoryEntity(
                title = title.ifBlank { url },
                url = url
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = db.historyDao().deleteById(id)

    suspend fun clearHistory() = db.historyDao().clearAll()

    suspend fun insertDownload(item: DownloadEntity): Long = db.downloadDao().insert(item)

    suspend fun updateDownloadBySystemId(
        systemId: Long,
        status: DownloadStatus,
        downloadedBytes: Long,
        totalBytes: Long,
        localUri: String?
    ) {
        db.downloadDao().updateBySystemId(
            systemId = systemId,
            status = status.name,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            localUri = localUri
        )
    }

    suspend fun deleteDownload(id: Long) = db.downloadDao().deleteById(id)

    suspend fun clearDownloads() = db.downloadDao().clearAll()

    suspend fun getSavedTabs(): List<SavedTabEntity> = db.savedTabDao().getAll()

    suspend fun saveTabsSnapshot(tabs: List<SavedTabEntity>) {
        db.savedTabDao().clearAll()
        if (tabs.isNotEmpty()) {
            db.savedTabDao().insertAll(tabs)
        }
    }

    suspend fun clearSavedTabs() = db.savedTabDao().clearAll()
}

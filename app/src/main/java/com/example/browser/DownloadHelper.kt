package com.example.browser

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DownloadHelper(
    private val context: Context,
    private val repository: BrowserRepository,
    private val scope: CoroutineScope
) {
    private val downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueueDownload(
        url: String,
        suggestedFileName: String? = null,
        mimeType: String? = null,
        userAgent: String? = null
    ): Long {
        val fileName = (suggestedFileName ?: UrlUtils.guessFileName(url, null))
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .ifBlank { "download_${System.currentTimeMillis()}" }

        val resolvedMime = mimeType?.takeIf { it.isNotBlank() } ?: run {
            val ext = MimeTypeMap.getFileExtensionFromUrl(url)
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription("MeoMeo Browser")
            setMimeType(resolvedMime)
            if (!userAgent.isNullOrBlank()) {
                addRequestHeader("User-Agent", userAgent)
            }
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }

        val systemId = downloadManager.enqueue(request)
        scope.launch(Dispatchers.IO) {
            repository.insertDownload(
                DownloadEntity(
                    systemDownloadId = systemId,
                    fileName = fileName,
                    url = url,
                    mimeType = resolvedMime,
                    status = DownloadStatus.DOWNLOADING.name
                )
            )
            monitorDownload(systemId)
        }
        return systemId
    }

    private suspend fun monitorDownload(systemId: Long) {
        var finished = false
        var attempts = 0
        while (!finished && attempts < 300) {
            attempts++
            val query = DownloadManager.Query().setFilterById(systemId)
            var cursor: Cursor? = null
            try {
                cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val uriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                    val dmStatus = if (statusIdx >= 0) cursor.getInt(statusIdx) else DownloadManager.STATUS_RUNNING
                    val downloaded = if (downloadedIdx >= 0) cursor.getLong(downloadedIdx) else 0L
                    val total = if (totalIdx >= 0) cursor.getLong(totalIdx) else -1L
                    val localUri = if (uriIdx >= 0) cursor.getString(uriIdx) else null

                    val mappedStatus = when (dmStatus) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            finished = true
                            DownloadStatus.COMPLETED
                        }
                        DownloadManager.STATUS_FAILED -> {
                            finished = true
                            DownloadStatus.FAILED
                        }
                        DownloadManager.STATUS_PENDING -> DownloadStatus.PENDING
                        else -> DownloadStatus.DOWNLOADING
                    }

                    val contentUri = if (mappedStatus == DownloadStatus.COMPLETED) {
                        downloadManager.getUriForDownloadedFile(systemId)?.toString() ?: localUri
                    } else localUri

                    repository.updateDownloadBySystemId(
                        systemId = systemId,
                        status = mappedStatus,
                        downloadedBytes = downloaded,
                        totalBytes = total,
                        localUri = contentUri
                    )
                } else {
                    finished = true
                }
            } catch (_: Exception) {
                finished = true
            } finally {
                cursor?.close()
            }
            if (!finished) delay(700L)
        }
    }

    fun openDownloadedFile(item: DownloadEntity): Boolean {
        return try {
            val uri = if (item.systemDownloadId > 0) {
                downloadManager.getUriForDownloadedFile(item.systemDownloadId)
                    ?: item.localUri?.let { Uri.parse(it) }
            } else {
                item.localUri?.let { Uri.parse(it) }
            } ?: return false

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, item.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}

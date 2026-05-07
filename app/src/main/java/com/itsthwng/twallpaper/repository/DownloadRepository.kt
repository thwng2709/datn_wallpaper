package com.itsthwng.twallpaper.repository

import android.content.Context
import com.itsthwng.twallpaper.utils.download.DownloadManager
import com.itsthwng.twallpaper.utils.download.DownloadState
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadManager: DownloadManager
) {

    /**
     * Download ảnh từ URL
     */
    fun downloadImage(
        context: Context,
        url: String,
        fileName: String? = null,
        type: String
    ): Flow<DownloadState> {
        return downloadManager.downloadImage(context, url, fileName, type)
    }

    /**
     * Lấy danh sách file đã download theo type
     */
    fun getDownloadedFiles(context: Context, type: String): List<File> {
        return downloadManager.getDownloadedFiles(context, type)
    }

    /**
     * Xóa file đã download
     */
    fun deleteDownloadedFile(context: Context, filePath: String): Boolean {
        return downloadManager.deleteDownloadedFile(context, filePath)
    }

    /**
     * Kiểm tra file đã được download chưa
     */
    fun isFileDownloaded(context: Context, url: String, type: String): Boolean {
        return downloadManager.isFileDownloaded(context, url, type)
    }

    /**
     * Kiểm tra file đã được download chưa theo ID
     */
    fun isFileDownloadedById(context: Context, id: Int, type: String, url: String): Boolean {
        return downloadManager.isFileDownloadedById(context, id, type, url)
    }

    /**
     * Lấy tổng số file đã download theo type
     */
    fun getDownloadedFileCount(context: Context, type: String): Int {
        return downloadManager.getDownloadedFiles(context, type).size
    }

    /**
     * Lấy tổng dung lượng đã download theo type (bytes)
     */
    fun getDownloadedSize(context: Context, type: String): Long {
        return downloadManager.getDownloadedFiles(context, type)
            .sumOf { it.length() }
    }

    /**
     * Lấy danh sách tất cả file đã download
     */
    fun getAllDownloadedFiles(context: Context): Map<String, List<File>> {
        val types = listOf(
            DownloadManager.FOLDER_ZIPPER_IMAGE,
            DownloadManager.FOLDER_ZIPPERS,
            DownloadManager.FOLDER_CHAINS
        )
        
        return types.associateWith { type ->
            downloadManager.getDownloadedFiles(context, type)
        }
    }

    /**
     * Xóa tất cả file đã download theo type
     */
    fun clearDownloadedFiles(context: Context, type: String): Boolean {
        return try {
            val files = downloadManager.getDownloadedFiles(context, type)
            files.all { file ->
                downloadManager.deleteDownloadedFile(context, file.absolutePath)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Xóa tất cả file đã download
     */
    fun clearAllDownloadedFiles(context: Context): Boolean {
        val types = listOf(
            DownloadManager.FOLDER_ZIPPER_IMAGE,
            DownloadManager.FOLDER_ZIPPERS,
            DownloadManager.FOLDER_CHAINS
        )
        
        return types.all { type ->
            clearDownloadedFiles(context, type)
        }
    }
}


package com.itsthwng.twallpaper.utils.download

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor() {

    companion object {
        private const val TAG = "DownloadManager"
        
        // Các folder tương ứng với từng type
        const val FOLDER_ZIPPER_IMAGE = "zipper_images"
        const val FOLDER_ZIPPERS = "zippers" 
        const val FOLDER_CHAINS = "chains"
    }

    /**
     * Download ảnh từ URL và lưu theo type
     * @param context Context của ứng dụng
     * @param url URL của ảnh cần download
     * @param fileName Tên file (nếu null sẽ tự động tạo)
     * @param type Loại ảnh (ZIPPER_IMAGE, ZIPPERS, CHAINS, WALLPAPERS)
     * @return Flow DownloadState để theo dõi tiến trình
     */
    fun downloadImage(
        context: Context,
        url: String,
        fileName: String? = null,
        type: String
    ): Flow<DownloadState> = flow {
        try {
            emit(DownloadState.Downloading(0))
            
            // Tạo tên file nếu không có
            val finalFileName = fileName ?: generateFileName(url, type)
            
            // Kiểm tra file đã tồn tại
            val existingFile = getExistingFile(context, finalFileName, type)
            if (existingFile != null && existingFile.exists()) {
                emit(DownloadState.AlreadyExists(existingFile.absolutePath))
                return@flow
            }
            
            // Sử dụng Android DownloadManager
            val downloadId = startDownload(context, url, finalFileName, type)
            
            // Emit 100% khi hoàn thành
            emit(DownloadState.Downloading(100))
            
            // Lưu thông tin download vào database
            saveDownloadInfo(context, url, finalFileName, type)
            
            emit(DownloadState.Success(finalFileName))
            
        } catch (e: Exception) {
            Logger.e(e, "Download failed for URL: $url")
            emit(DownloadState.Failed(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Lấy folder tương ứng với type
     */
    private fun getFolderByType(context: Context, type: String): File {
        // Sử dụng Android DownloadManager directory
        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return when (type.uppercase()) {
            "ZIPPER_IMAGE" -> File(baseDir, FOLDER_ZIPPER_IMAGE)
            "ZIPPERS" -> File(baseDir, FOLDER_ZIPPERS)
            "CHAINS" -> File(baseDir, FOLDER_CHAINS)
            else -> File(baseDir, FOLDER_ZIPPER_IMAGE) // Default folder
        }
    }

    /**
     * Tạo tên file tự động
     */
    private fun generateFileName(url: String, type: String): String {
        val extension = getFileExtension(url)
        return "${type.lowercase()}.$extension"
    }

    /**
     * Tạo tên file theo ID
     */
    fun generateFileNameById(id: Int, type: String, url: String): String {
        val extension = getFileExtension(url)
        return "${type.lowercase()}_${id}.$extension"
    }

    /**
     * Tạo tên file theo ID
     */
    fun generateFileNameByIdChains(id: Int, type: String, url: String, isLeft: Boolean): String {
        val extension = getFileExtension(url)
        val side = if (isLeft) "left" else "right"
        return "${type.lowercase()}_${id}_${side}.$extension"
    }

    /**
     * Lấy extension của file từ URL
     */
    private fun getFileExtension(url: String): String {
        return try {
            val lastDotIndex = url.lastIndexOf('.')
            if (lastDotIndex != -1 && lastDotIndex < url.length - 1) {
                url.substring(lastDotIndex + 1).lowercase()
            } else {
                "jpg" // Default extension
            }
        } catch (e: Exception) {
            "jpg"
        }
    }

    /**
     * Bắt đầu download sử dụng Android DownloadManager
     */
    private fun startDownload(context: Context, url: String, fileName: String, type: String): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        
        // Tạo folder nếu chưa có
        val folder = getFolderByType(context, type)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        
        // Sử dụng folder name thay vì type để tránh nhầm lẫn
        val folderName = when (type.uppercase()) {
            "ZIPPER_IMAGE" -> FOLDER_ZIPPER_IMAGE
            "ZIPPERS" -> FOLDER_ZIPPERS
            "CHAINS" -> FOLDER_CHAINS
            else -> FOLDER_ZIPPER_IMAGE
        }
        
        val request = android.app.DownloadManager.Request(Uri.parse(url))
            .setTitle(context.getString(R.string.download_image_title, type))
            .setDescription(context.getString(R.string.download_image_description, url))
            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$folderName/$fileName")
            .setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
        
        return downloadManager.enqueue(request)
    }

    /**
     * Lưu thông tin download vào database (sẽ implement sau)
     */
    private fun saveDownloadInfo(context: Context, url: String, fileName: String, type: String) {
        // TODO: Implement database save
        Log.d(TAG, "Download info saved: URL=$url, FileName=$fileName, Type=$type")
    }

    /**
     * Kiểm tra file đã tồn tại
     */
    private fun getExistingFile(context: Context, fileName: String, type: String): File? {
        val folder = getFolderByType(context, type)
        val file = File(folder, fileName)
        return if (file.exists()) file else null
    }

    /**
     * Lấy danh sách file đã download theo type
     */
    fun getDownloadedFiles(context: Context, type: String): List<File> {
        val folder = getFolderByType(context, type)
        return if (folder.exists()) {
            folder.listFiles()?.filter { it.isFile } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Xóa file đã download
     */
    fun deleteDownloadedFile(context: Context, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "File deleted successfully: $filePath")
                }
                deleted
            } else {
                false
            }
        } catch (e: Exception) {
            Logger.e(e, "Failed to delete file: $filePath")
            false
        }
    }

    /**
     * Kiểm tra file đã tồn tại
     */
    fun isFileDownloaded(context: Context, url: String, type: String): Boolean {
        val fileName = generateFileName(url, type)
        val existingFile = getExistingFile(context, fileName, type)
        return existingFile != null && existingFile.exists()
    }

    /**
     * Kiểm tra file đã tồn tại theo ID
     */
    fun isFileDownloadedById(context: Context, id: Int, type: String, url: String): Boolean {
        val fileName = if (type.uppercase() == "CHAINS") {
            // Với chains, cần xác định đây là left hay right dựa vào URL
            // Tạm thời sử dụng generateFileNameById cũ để tương thích
            generateFileNameById(id, type, url)
        } else {
            generateFileNameById(id, type, url)
        }
        
        val existingFile = getExistingFile(context, fileName, type)
        
        // Log để debug
        Log.d(TAG, "Checking file by ID: ID=$id, Type=$type, FileName=$fileName, Exists=${existingFile?.exists()}")
        
        return existingFile != null && existingFile.exists()
    }

    /**
     * Kiểm tra file chain đã tồn tại theo ID và loại (left/right)
     */
    fun isChainFileDownloadedById(context: Context, id: Int, type: String, url: String, isLeft: Boolean): Boolean {
        val fileName = generateFileNameByIdChains(id, type, url, isLeft)
        val existingFile = getExistingFile(context, fileName, type)
        
        // Log để debug
        Log.d(TAG, "Checking chain file by ID: ID=$id, Type=$type, IsLeft=$isLeft, FileName=$fileName, Exists=${existingFile?.exists()}")
        
        return existingFile != null && existingFile.exists()
    }

    /**
     * Download ảnh với callback pattern (dành cho Java)
     */
    fun downloadImageWithCallback(
        context: Context,
        url: String,
        fileName: String? = null,
        type: String,
        callback: DownloadCallback
    ) {
        try {
            callback.onProgress(0)
            
            // Tạo tên file nếu không có
            val finalFileName = fileName ?: generateFileName(url, type)
            
            // Kiểm tra file đã tồn tại
            val existingFile = getExistingFile(context, finalFileName, type)
            if (existingFile != null && existingFile.exists()) {
                callback.onSuccess(existingFile.absolutePath)
                return
            }
            
            // Sử dụng Android DownloadManager
            val downloadId = startDownload(context, url, finalFileName, type)
            
            callback.onProgress(100)
            
            // Lưu thông tin download vào database
            saveDownloadInfo(context, url, finalFileName, type)
            
            // Trả về đường dẫn file
            val folder = getFolderByType(context, type)
            val filePath = File(folder, finalFileName).absolutePath
            callback.onSuccess(filePath)
            
        } catch (e: Exception) {
            Logger.e(e, "Download failed for URL: $url")
            callback.onFailed(e.message ?: "Download failed")
        }
    }

    /**
     * Download ảnh với callback pattern theo ID (dành cho Java)
     */
    fun downloadImageWithCallbackById(
        context: Context,
        id: Int,
        url: String,
        type: String,
        callback: DownloadCallback
    ) {
        try {
            callback.onProgress(0)
            
            // Tạo tên file theo ID
            val fileName = generateFileNameById(id, type, url)
            Log.d(TAG, "Starting download by ID: ID=$id, Type=$type, FileName=$fileName, URL=$url")
            
            // Kiểm tra file đã tồn tại
            val existingFile = getExistingFile(context, fileName, type)
            if (existingFile != null && existingFile.exists()) {
                Log.d(TAG, "File already exists, skipping download: ${existingFile.absolutePath}")
                callback.onSuccess(existingFile.absolutePath)
                return
            }
            
            Log.d(TAG, "File not found, starting download...")
            
            // Sử dụng Android DownloadManager
            val downloadId = startDownload(context, url, fileName, type)
            
            callback.onProgress(100)
            
            // Lưu thông tin download vào database
            saveDownloadInfo(context, url, fileName, type)
            
            // Trả về đường dẫn file
            val folder = getFolderByType(context, type)
            val filePath = File(folder, fileName).absolutePath
            Log.d(TAG, "Download completed, file path: $filePath")
            callback.onSuccess(filePath)
            
        } catch (e: Exception) {
            Logger.e(e, "Download failed for URL: $url")
            callback.onFailed(e.message ?: "Download failed")
        }
    }

    /**
     * Download nhiều ảnh cùng lúc với callback pattern (dành cho Java)
     * @param context Context của ứng dụng
     * @param id ID của item
     * @param urls Danh sách URL cần download
     * @param type Loại ảnh
     * @param callback Callback để theo dõi tiến trình
     */
    fun downloadMultipleImagesWithCallback(
        context: Context,
        id: Int,
        urls: List<String>,
        type: String,
        callback: MultipleDownloadCallback
    ) {
        if (urls.isEmpty()) {
            callback.onFailed("Không có URL nào để download")
            return
        }

        try {
            callback.onProgress(0)
            
            val totalUrls = urls.size
            var completedDownloads = 0
            var failedDownloads = 0
            val downloadedFiles = mutableListOf<String>()
            val errors = mutableListOf<String>()
            
            Log.d(TAG, "Starting multiple downloads: ID=$id, Type=$type, Total URLs=$totalUrls")
            
            // Kiểm tra các file đã tồn tại trước
            val existingFiles = mutableListOf<String>()
            for (url in urls) {
                val fileName = generateFileNameById(id, type, url)
                val existingFile = getExistingFile(context, fileName, type)
                if (existingFile != null && existingFile.exists()) {
                    existingFiles.add(existingFile.absolutePath)
                    completedDownloads++
                    Log.d(TAG, "File already exists: ${existingFile.absolutePath}")
                }
            }
            
            // Nếu tất cả file đã tồn tại
            if (completedDownloads == totalUrls) {
                callback.onProgress(100)
                callback.onSuccess(existingFiles)
                return
            }
            
            // Download các file còn thiếu
            for ((index, url) in urls.withIndex()) {
                val fileName = generateFileNameById(id, type, url)
                val existingFile = getExistingFile(context, fileName, type)
                
                if (existingFile != null && existingFile.exists()) {
                    // File đã tồn tại, bỏ qua
                    continue
                }
                
                try {
                    Log.d(TAG, "Downloading file $index: $url")
                    
                    // Sử dụng Android DownloadManager
                    val downloadId = startDownload(context, url, fileName, type)
                    
                    // Lưu thông tin download vào database
                    saveDownloadInfo(context, url, fileName, type)
                    
                    // Trả về đường dẫn file
                    val folder = getFolderByType(context, type)
                    val filePath = File(folder, fileName).absolutePath
                    downloadedFiles.add(filePath)
                    
                    completedDownloads++
                    Log.d(TAG, "Download completed for file $index: $filePath")
                    
                } catch (e: Exception) {
                    failedDownloads++
                    val errorMsg = "Download failed for URL $url: ${e.message}"
                    errors.add(errorMsg)
                    Log.e(TAG, errorMsg, e)
                }
                
                // Cập nhật progress
                val progress = (completedDownloads * 100) / totalUrls
                callback.onProgress(progress)
            }
            
            // Xử lý kết quả cuối cùng
            if (failedDownloads == 0) {
                // Tất cả download thành công
                val allFiles = existingFiles + downloadedFiles
                callback.onSuccess(allFiles)
            } else if (completedDownloads > 0) {
                // Một số file thành công, một số thất bại
                val allFiles = existingFiles + downloadedFiles
                callback.onPartialSuccess(allFiles, errors)
            } else {
                // Tất cả download thất bại
                callback.onFailed("Tất cả download đều thất bại: ${errors.joinToString(", ")}")
            }
            
        } catch (e: Exception) {
            val errorMsg = "Multiple download failed: ${e.message}"
            Log.e(TAG, errorMsg, e)
            callback.onFailed(errorMsg)
        }
    }

    /**
     * Download 2 ảnh chain (left và right) cùng lúc với callback pattern
     * @param context Context của ứng dụng
     * @param id ID của chain
     * @param leftUrl URL của ảnh chain left
     * @param rightUrl URL của ảnh chain right
     * @param type Loại ảnh (thường là "CHAINS")
     * @param callback Callback để theo dõi tiến trình
     */
    fun downloadChainImagesWithCallback(
        context: Context,
        id: Int,
        leftUrl: String?,
        rightUrl: String?,
        type: String,
        callback: ChainDownloadCallback
    ) {
        if (leftUrl.isNullOrEmpty() && rightUrl.isNullOrEmpty()) {
            callback.onFailed("Không có URL nào để download")
            return
        }
        
        Log.d(TAG, "Starting chain download: ID=$id, Left URL=$leftUrl, Right URL=$rightUrl")
        
        try {
            callback.onProgress(0)
            
            var completedDownloads = 0
            var failedDownloads = 0
            val downloadedFiles = mutableListOf<String>()
            val errors = mutableListOf<String>()
            
            val totalDownloads = (if (!leftUrl.isNullOrEmpty()) 1 else 0) + (if (!rightUrl.isNullOrEmpty()) 1 else 0)
            
            // Download left image nếu có
            if (!leftUrl.isNullOrEmpty()) {
                try {
                    val leftFileName = generateFileNameByIdChains(id, type, leftUrl, true)
                    Log.d(TAG, "Downloading left image: $leftFileName")
                    
                    // Kiểm tra file đã tồn tại
                    val existingLeftFile = getExistingFile(context, leftFileName, type)
                    if (existingLeftFile != null && existingLeftFile.exists()) {
                        Log.d(TAG, "Left file already exists: ${existingLeftFile.absolutePath}")
                        downloadedFiles.add(existingLeftFile.absolutePath)
                        completedDownloads++
                    } else {
                        // Download left image
                        val downloadId = startDownload(context, leftUrl, leftFileName, type)
                        Log.d(TAG, "Left image download started with ID: $downloadId")
                        
                        // Lưu thông tin download vào database
                        saveDownloadInfo(context, leftUrl, leftFileName, type)
                        
                        // Trả về đường dẫn file
                        val folder = getFolderByType(context, type)
                        val filePath = File(folder, leftFileName).absolutePath
                        downloadedFiles.add(filePath)
                        completedDownloads++
                        Log.d(TAG, "Left image download completed: $filePath")
                    }
                } catch (e: Exception) {
                    failedDownloads++
                    val errorMsg = "Left image download failed: ${e.message}"
                    errors.add(errorMsg)
                    Log.e(TAG, errorMsg, e)
                }
            }
            
            // Download right image nếu có
            if (!rightUrl.isNullOrEmpty()) {
                try {
                    val rightFileName = generateFileNameByIdChains(id, type, rightUrl, false)
                    Log.d(TAG, "Downloading right image: $rightFileName")
                    
                    // Kiểm tra file đã tồn tại
                    val existingRightFile = getExistingFile(context, rightFileName, type)
                    if (existingRightFile != null && existingRightFile.exists()) {
                        Log.d(TAG, "Right file already exists: ${existingRightFile.absolutePath}")
                        downloadedFiles.add(existingRightFile.absolutePath)
                        completedDownloads++
                    } else {
                        // Download right image
                        val downloadId = startDownload(context, rightUrl, rightFileName, type)
                        Log.d(TAG, "Right image download started with ID: $downloadId")
                        
                        // Lưu thông tin download vào database
                        saveDownloadInfo(context, rightUrl, rightFileName, type)
                        
                        // Trả về đường dẫn file
                        val folder = getFolderByType(context, type)
                        val filePath = File(folder, rightFileName).absolutePath
                        downloadedFiles.add(filePath)
                        completedDownloads++
                        Log.d(TAG, "Right image download completed: $filePath")
                    }
                } catch (e: Exception) {
                    failedDownloads++
                    val errorMsg = "Right image download failed: ${e.message}"
                    errors.add(errorMsg)
                    Log.e(TAG, errorMsg, e)
                }
            }
            
            // Cập nhật progress
            val progress = (completedDownloads * 100) / totalDownloads
            callback.onProgress(progress)
            
            // Xử lý kết quả cuối cùng
            if (failedDownloads == 0) {
                // Tất cả download thành công
                val leftFile = if (!leftUrl.isNullOrEmpty()) {
                    downloadedFiles.find { it.contains(generateFileNameByIdChains(id, type, leftUrl, true)) }
                } else null
                
                val rightFile = if (!rightUrl.isNullOrEmpty()) {
                    downloadedFiles.find { it.contains(generateFileNameByIdChains(id, type, rightUrl, false)) }
                } else null
                
                Log.d(TAG, "All chain downloads successful - Left: $leftFile, Right: $rightFile")
                callback.onSuccess(leftFile, rightFile)
            } else if (completedDownloads > 0) {
                // Một số file thành công, một số thất bại
                val leftFile = if (!leftUrl.isNullOrEmpty()) {
                    downloadedFiles.find { it.contains(generateFileNameByIdChains(id, type, leftUrl, true)) }
                } else null
                
                val rightFile = if (!rightUrl.isNullOrEmpty()) {
                    downloadedFiles.find { it.contains(generateFileNameByIdChains(id, type, rightUrl, false)) }
                } else null
                
                Log.d(TAG, "Partial chain downloads - Left: $leftFile, Right: $rightFile, Errors: $errors")
                callback.onPartialSuccess(leftFile, rightFile, errors)
            } else {
                // Tất cả download thất bại
                val errorMsg = "Tất cả chain downloads đều thất bại: ${errors.joinToString(", ")}"
                Log.e(TAG, errorMsg)
                callback.onFailed(errorMsg)
            }
            
        } catch (e: Exception) {
            val errorMsg = "Chain download failed: ${e.message}"
            Log.e(TAG, errorMsg, e)
            callback.onFailed(errorMsg)
        }
    }


}

/**
 * Interface callback cho download (dành cho Java)
 */
interface DownloadCallback {
    fun onProgress(progress: Int)
    fun onSuccess(filePath: String)
    fun onFailed(error: String)
}

/**
 * Interface callback cho multiple download (dành cho Java)
 */
interface MultipleDownloadCallback {
    fun onProgress(progress: Int)
    fun onSuccess(filePaths: List<String>)
    fun onPartialSuccess(filePaths: List<String>, errors: List<String>)
    fun onFailed(error: String)
}

/**
 * Interface callback cho chain download (dành cho Java)
 */
interface ChainDownloadCallback {
    fun onProgress(progress: Int)
    fun onSuccess(leftFile: String?, rightFile: String?)
    fun onPartialSuccess(leftFile: String?, rightFile: String?, errors: List<String>)
    fun onFailed(error: String)
}

/**
 * Trạng thái download
 */
sealed class DownloadState {
    data class Downloading(val progress: Int) : DownloadState()
    data class Success(val filePath: String) : DownloadState()
    data class AlreadyExists(val filePath: String) : DownloadState()
    data class Failed(val error: String) : DownloadState()
    object CancelDownload : DownloadState()
}

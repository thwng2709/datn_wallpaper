package com.itsthwng.twallpaper.utils.wallpaper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.OnProgressListener
import com.downloader.PRDownloader
import com.downloader.Progress
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.utils.PermissionManager
import com.itsthwng.twallpaper.utils.UrlHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Implementation of WallpaperDownloader
 * Handles both image and video wallpaper downloads
 */
class WallpaperDownloaderImpl @Inject constructor(
    private val context: Context
) : WallpaperDownloader {
    
    private var currentDownloadId: Int? = null
    
    override suspend fun download(
        wallpaper: SettingData.WallpapersItem,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        println("WallpaperDownloader: Starting download for wallpaper: ${wallpaper.content}")
        println("WallpaperDownloader: Wallpaper type: ${wallpaper.wallpaperType}")
        
        // Check permissions first
        if (!PermissionManager.hasStoragePermission(context)) {
            println("WallpaperDownloader: ERROR - No storage permission")
            onError(context.getString(R.string.permission_required_to_save_images))
            return
        }
        
        try {
            if (wallpaper.wallpaperType == 0) {
                // Image wallpaper
                downloadImage(wallpaper, onProgress, onComplete, onError)
            } else {
                // Video wallpaper
                downloadVideo(wallpaper, onProgress, onComplete, onError)
            }
        } catch (e: Exception) {
            println("WallpaperDownloader: ERROR in download - ${e.message}")
            e.printStackTrace()
            onError(e.message ?: context.getString(R.string.something_went_wrong))
        }
    }
    
    override fun cancelDownload() {
        currentDownloadId?.let {
            PRDownloader.cancel(it)
            currentDownloadId = null
        }
    }
    
    override fun isDownloading(): Boolean {
        return currentDownloadId != null
    }
    
    private suspend fun downloadImage(
        wallpaper: SettingData.WallpapersItem,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val imageUrl = UrlHelper.getFullUrl(wallpaper.content)
            println("WallpaperDownloader: Downloading image from URL: $imageUrl")
            
            val bitmap = loadBitmapFromUrl(imageUrl)
            if (bitmap != null) {
                println("WallpaperDownloader: Bitmap loaded successfully, size: ${bitmap.width}x${bitmap.height}")
                saveImageToStorage(bitmap, onComplete, onError)
            } else {
                println("WallpaperDownloader: ERROR - Failed to load bitmap from URL")
                withContext(Dispatchers.Main) {
                    onError(context.getString(R.string.error_saving_image))
                }
            }
        } catch (e: Exception) {
            println("WallpaperDownloader: ERROR in downloadImage - ${e.message}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError(e.message ?: context.getString(R.string.something_went_wrong))
            }
        }
    }
    
    private suspend fun downloadVideo(
        wallpaper: SettingData.WallpapersItem,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val fileURL = UrlHelper.getFullUrl(wallpaper.content)
        println("WallpaperDownloader: Downloading video from URL: $fileURL")
        val fileName = URLUtil.guessFileName(fileURL, null, MimeTypeMap.getFileExtensionFromUrl(fileURL))
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val path = downloadDir.absolutePath
        
        val file = File("$path/$fileName")
        if (file.exists()) {
            file.delete()
        }
        
        currentDownloadId = PRDownloader.download(fileURL, path, fileName)
            .build()
            .setOnProgressListener(object : OnProgressListener {
                override fun onProgress(progress: Progress) {
                    val percentage = ((progress.currentBytes * 100) / progress.totalBytes).toInt()
                    onProgress(percentage)
                }
            })
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    currentDownloadId = null
                    MediaScannerConnection.scanFile(
                        context, 
                        arrayOf("$path/$fileName"), 
                        null
                    ) { _, _ -> }
                    onComplete()
                }
                
                override fun onError(error: Error?) {
                    currentDownloadId = null
                    onError(context.getString(R.string.something_went_wrong))
                }
            })
    }
    
    
    private fun loadBitmapFromUrl(urlString: String): Bitmap? {
        return try {
            println("WallpaperDownloader: loadBitmapFromUrl - Opening connection to: $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            
            val responseCode = connection.responseCode
            println("WallpaperDownloader: HTTP Response Code: $responseCode")
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                println("WallpaperDownloader: ERROR - HTTP error code: $responseCode")
                return null
            }
            
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            val bitmap = BitmapFactory.decodeStream(bufferedInputStream)
            
            if (bitmap == null) {
                println("WallpaperDownloader: ERROR - BitmapFactory.decodeStream returned null")
            } else {
                println("WallpaperDownloader: Bitmap decoded successfully, size: ${bitmap.width}x${bitmap.height}")
            }
            
            bitmap
        } catch (e: Exception) {
            println("WallpaperDownloader: ERROR in loadBitmapFromUrl - ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    private suspend fun saveImageToStorage(
        bitmap: Bitmap,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        println("WallpaperDownloader: saveImageToStorage - Starting to save image: $filename")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                println("WallpaperDownloader: Using MediaStore for Android Q+")
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                    contentValues
                )
                println("WallpaperDownloader: Created image URI: $imageUri")
                fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                println("WallpaperDownloader: Using traditional file system for Android < Q")
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                println("WallpaperDownloader: Pictures directory: ${imagesDir.absolutePath}")
                
                if (!imagesDir.exists()) {
                    println("WallpaperDownloader: Creating Pictures directory...")
                    val created = imagesDir.mkdirs()
                    println("WallpaperDownloader: Directory created: $created")
                }
                
                val imageFile = File(imagesDir, filename)
                println("WallpaperDownloader: Saving to file: ${imageFile.absolutePath}")
                fos = FileOutputStream(imageFile)
            }
            
            fos?.use {
                println("WallpaperDownloader: Compressing bitmap to JPEG...")
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                println("WallpaperDownloader: Bitmap compressed successfully")
                
                // For Android < Q, notify MediaScanner to make image visible in Gallery
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val imageFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        filename
                    )
                    println("WallpaperDownloader: Notifying MediaScanner for file: ${imageFile.absolutePath}")
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(imageFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )
                }
                
                println("WallpaperDownloader: Image saved successfully!")
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } ?: withContext(Dispatchers.Main) {
                println("WallpaperDownloader: ERROR - OutputStream is null")
                onError(context.getString(R.string.error_saving_image))
            }
        } catch (e: SecurityException) {
            println("WallpaperDownloader: ERROR - SecurityException: ${e.message}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                // Provide more specific error message based on the actual permission issue
                val errorMsg = when {
                    !PermissionManager.hasStoragePermission(context) -> 
                        context.getString(R.string.permission_required_to_save_images)
                    else -> 
                        context.getString(R.string.storage_access_denied)
                }
                onError(errorMsg)
            }
        } catch (e: IOException) {
            println("WallpaperDownloader: ERROR - IOException: ${e.message}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError(context.getString(R.string.error_network_connection))
            }
        } catch (e: Exception) {
            println("WallpaperDownloader: ERROR - Exception in saveImageToStorage: ${e.message}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError(context.getString(R.string.error_saving_image))
            }
        }
    }
}
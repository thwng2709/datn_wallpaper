package com.itsthwng.twallpaper.repository

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.*
import java.text.DecimalFormat
import javax.inject.Inject

class FileHelperImpl @Inject constructor() : FileHelper {
    private var cancelDownload = false

    override fun getStringFromStream(_is: InputStream?): String {
        if (_is == null) {
            return ""
        }
        val reader = BufferedReader(InputStreamReader(_is))
        val sb = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
                sb.append("\n")
            }
        } catch (e: IOException) {
            Logger.e(e, "IOException")
        } finally {
            try {
                _is.close()
            } catch (e: IOException) {
                Logger.e(e, "IOException")
            }
        }
        return sb.toString()
    }

    override fun checkPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result: Int = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val result1: Int = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun shareFile(context: Context, filePath: String) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val fileWithinMyDir = File(filePath)

        if (fileWithinMyDir.exists()) {
            intentShareFile.type = "application/*"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath))
            intentShareFile.putExtra(
                Intent.EXTRA_SUBJECT, "Sharing File..."
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            context.startActivity(Intent.createChooser(intentShareFile, "Share File"))
        }
    }

    override fun sharePdfFile(activity: Activity, uri: Uri) {
        try {
            val intent = ShareCompat.IntentBuilder.from(activity)
                .setType(activity.contentResolver.getType(uri)).setStream(uri).intent
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val createChooser = Intent.createChooser(intent, "Sharing File...")
            createChooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (createChooser.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(createChooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, activity.getString(R.string.cant_share_file), Toast.LENGTH_LONG).show()
        }
    }

    override fun getFileType(path: String): String {
        return when {
            path.contains(".pdf") -> {
                "application/pdf"
            }

            path.contains(".ppt") -> {
                "application/ppt"
            }

            path.contains(".xls") -> {
                "application/xls"
            }

            path.contains(".doc") -> {
                "application/doc"
            }

            path.contains(".txt") -> {
                "application/txt"
            }

            else -> "application/txt"
        }
    }

    override fun convertFileTypeToText(type: String): String {
        return when {
            type.contains("application/pdf") -> "pdf"
            type.contains("application/ppt") -> "ppt"
            type.contains("application/xls") -> "xls"
            type.contains("application/doc") -> "doc"
            type.contains("application/txt") -> "txt"
            else -> "txt"
        }
    }

    override fun getStringSizeLengthFile(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        return when {
            size < sizeMb -> df.format(size / sizeKb).toString() + " Kb"

            size < sizeGb -> df.format(size / sizeMb).toString() + " Mb"

            size < sizeTerra -> df.format(size / sizeGb).toString() + " Gb"

            else -> ""
        }
    }

    override fun getStringCountRoundingFile(count: Int): String {
        return if (count >= 1000) {
            val num = count.toFloat() / 1000
            val df = DecimalFormat("#.##")
            df.format(num) + "k"
        } else count.toString()
    }

    override fun getDirFile(): File {
        val dir = File(Environment.getExternalStorageDirectory(), "FileReader")
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir
    }

    override fun downloadFile(
        url: String, context: Context, filename: String
    ): Flow<DownloadState>? {
        val retryApiRecord = 0
        try {
            val internalStorage = context.filesDir
            cancelDownload = false

            val path = getDirFile().path + "/" + filename
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            return if (response.body != null && response.body!!.contentLength() > 0) {
                response.body?.saveFile(path)
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun cancelDownload() {
        cancelDownload = true
    }

    override fun getRealPath(context: Context, fileUri: Uri): String? {
        return getRealPathFromURI_API19(context, fileUri)
    }

    @SuppressLint("NewApi")
    override fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                // This is for checking Main Memory
                return if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        Environment.getExternalStorageDirectory().toString() + "/"
                    }
                    // This is for checking SD Card
                } else {
                    "storage" + "/" + docId.replace(":", "/")
                }
            } else if (isDownloadsDocument(uri)) {
                val fileName = getFilePath(context, uri)
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + fileName
                }
                var id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:".toRegex(), "")
                    val file = File(id)

                    if (file.exists()) return id
                }
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)

                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                if (contentUri == null) return null
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context, uri, null, null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    override fun getDataColumn(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    override fun getFilePath(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri, projection, null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    override fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    override fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    override fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    override fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    //----------------------------------------------------------------------------------------------
    private fun ResponseBody.saveFile(filePath: String): Flow<DownloadState> {
        return flow {
            emit(DownloadState.Downloading(0))
            val destinationFile = File(filePath)

            try {
                byteStream().use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        val totalBytes = contentLength()
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var progressBytes = 0L
                        var bytes = inputStream.read(buffer)
                        while (bytes >= 0) {
                            outputStream.write(buffer, 0, bytes)
                            progressBytes += bytes
                            bytes = inputStream.read(buffer)
                            emit(DownloadState.Downloading(((progressBytes * 100) / totalBytes).toInt()))
                            if (cancelDownload) {
                                emit(DownloadState.CancelDownload)
                                return@flow
                            }
                        }
                    }
                }
                emit(DownloadState.Finished)
            } catch (e: Exception) {
                emit(DownloadState.Failed(e))
            }
        }.flowOn(Dispatchers.IO).distinctUntilChanged()
    }

    sealed class DownloadState {
        data class Downloading(val progress: Int) : DownloadState()
        object Finished : DownloadState()
        data class Failed(val error: Throwable? = null) : DownloadState()
        object CancelDownload : DownloadState()
    }

}
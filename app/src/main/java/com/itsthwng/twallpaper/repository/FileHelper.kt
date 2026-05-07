package com.itsthwng.twallpaper.repository

import android.app.Activity
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream

interface FileHelper {
    fun getStringFromStream(_is: InputStream?): String
    fun checkPermission(context: Context):Boolean
    fun shareFile(context: Context, filePath: String)
    fun sharePdfFile(activity: Activity, uri: Uri)
    fun getFileType(path: String): String
    fun convertFileTypeToText(type: String): String
    fun getStringSizeLengthFile(size: Long): String
    fun getStringCountRoundingFile(count: Int): String
    fun getDirFile(): File
    fun downloadFile(
        url: String, context: Context, filename: String
    ): Flow<FileHelperImpl.DownloadState>?
    fun getRealPath(context: Context, fileUri: Uri): String?
    fun getRealPathFromURI_API19(context: Context, uri: Uri): String?
    fun getDataColumn(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String?
    fun getFilePath(context: Context, uri: Uri): String?
    fun isExternalStorageDocument(uri: Uri): Boolean
    fun isDownloadsDocument(uri: Uri): Boolean
    fun isMediaDocument(uri: Uri): Boolean
    fun isGooglePhotosUri(uri: Uri): Boolean
}
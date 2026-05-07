package com.itsthwng.twallpaper.ui.component.zipper

interface DownloadInternalCallback {
    fun onProgress(progress: Int) // 0..100
    fun onSuccess(filePath: String?) // Internal: absolute path; MediaStore Q+: content:// URI string
    fun onFailed(error: String?)
}

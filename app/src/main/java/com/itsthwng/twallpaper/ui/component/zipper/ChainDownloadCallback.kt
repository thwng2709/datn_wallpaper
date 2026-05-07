package com.itsthwng.twallpaper.ui.component.zipper

interface ChainDownloadCallback {
    fun onProgress(progress: Int) // 0..100 (trung bình 2 ảnh)
    fun onSuccess(leftPath: String?, rightPath: String?) // cả 2 đều OK (null nếu không có URL)
    fun onPartialSuccess(
        leftPath: String?,
        rightPath: String?,
        errors: MutableList<String?>?
    ) // 1 OK, 1 lỗi

    fun onFailed(error: String?) // cả 2 đều lỗi hoặc lỗi tổng quát
}

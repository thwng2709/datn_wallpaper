package com.itsthwng.twallpaper.ui.component.zipper

import java.util.Locale

class FileNameUtils {
    /**
     * Tạo tên file tự động
     */
    private fun generateFileName(url: String, type: String): String {
        val extension = getFileExtension(url)
        return type.lowercase(Locale.getDefault()) + "." + extension
    }

    /**
     * Tạo tên file theo ID
     */
    fun generateFileNameById(id: Int, type: String, url: String): String {
        val extension = getFileExtension(url)
        return type.lowercase(Locale.getDefault()) + "_" + id + "." + extension
    }

    /**
     * Tạo tên file theo ID cho Chains
     */
    fun generateFileNameByIdChains(id: Int, type: String, url: String, isLeft: Boolean): String {
        val extension = getFileExtension(url)
        val side = if (isLeft) "left" else "right"
        return type.lowercase(Locale.getDefault()) + "_" + id + "_" + side + "." + extension
    }

    /**
     * Lấy extension của file từ URL
     */
    private fun getFileExtension(url: String): String {
        return try {
            val lastDotIndex = url.lastIndexOf('.')
            if (lastDotIndex != -1 && lastDotIndex < url.length - 1) {
                url.substring(lastDotIndex + 1).lowercase(Locale.getDefault())
            } else {
                "jpg" // Default extension
            }
        } catch (e: Exception) {
            "jpg"
        }
    }

    companion object {
        private const val TAG = "DownloadManager"

        // Các folder tương ứng với từng type
        const val FOLDER_ZIPPER_IMAGE: String = "zipper_images"
        const val FOLDER_ZIPPERS: String = "zippers"
        const val FOLDER_CHAINS: String = "chains"
    }
}

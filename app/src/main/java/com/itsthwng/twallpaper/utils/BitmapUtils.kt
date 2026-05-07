package com.itsthwng.twallpaper.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView

/**
 * Utility class để xử lý bitmap an toàn và tránh lỗi recycled bitmap
 */
object BitmapUtils {
    
    private const val TAG = "BitmapUtils"
    
    /**
     * Kiểm tra xem bitmap có hợp lệ để sử dụng không
     */
    fun isBitmapValid(bitmap: Bitmap?): Boolean {
        return try {
            bitmap != null && !bitmap.isRecycled && bitmap.width > 0 && bitmap.height > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking bitmap validity: ${e.message}")
            false
        }
    }
    
    /**
     * Kiểm tra xem drawable có chứa bitmap hợp lệ không
     */
    fun isDrawableValid(drawable: Drawable?): Boolean {
        return try {
            when (drawable) {
                is BitmapDrawable -> isBitmapValid(drawable.bitmap)
                else -> drawable != null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking drawable validity: ${e.message}")
            false
        }
    }
    
    /**
     * Lấy bitmap từ drawable một cách an toàn
     */
    fun getBitmapSafely(drawable: Drawable?): Bitmap? {
        return try {
            when (drawable) {
                is BitmapDrawable -> {
                    val bitmap = drawable.bitmap
                    if (isBitmapValid(bitmap)) bitmap else null
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bitmap from drawable: ${e.message}")
            null
        }
    }
    
    /**
     * Kiểm tra xem ImageView có hợp lệ để sử dụng không
     */
    fun isImageViewValid(imageView: ImageView?): Boolean {
        return try {
            imageView != null && 
            imageView.context != null && 
            // Không thể gọi isFinishing trên Context, chỉ kiểm tra context có hợp lệ không
            imageView.width > 0 && 
            imageView.height > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking ImageView validity: ${e.message}")
            false
        }
    }
    
    /**
     * Xóa drawable của ImageView một cách an toàn
     */
    fun clearImageViewSafely(imageView: ImageView?) {
        try {
            if (isImageViewValid(imageView)) {
                val currentDrawable = imageView?.drawable
                if (currentDrawable is BitmapDrawable) {
                    val bitmap = currentDrawable.bitmap
                    if (isBitmapValid(bitmap)) {
                        // Không cần recycle bitmap vì Glide sẽ quản lý
                        imageView?.setImageDrawable(null)
                    }
                } else {
                    imageView?.setImageDrawable(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing ImageView: ${e.message}")
        }
    }
    
    /**
     * Kiểm tra và log trạng thái bitmap để debug
     */
    fun logBitmapStatus(tag: String, bitmap: Bitmap?, operation: String) {
        try {
            if (bitmap == null) {
                Log.d(TAG, "[$tag] Bitmap is null for operation: $operation")
            } else if (bitmap.isRecycled) {
                Log.w(TAG, "[$tag] Bitmap is recycled for operation: $operation")
            } else {
                Log.d(TAG, "[$tag] Bitmap is valid (${bitmap.width}x${bitmap.height}) for operation: $operation")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$tag] Error logging bitmap status: ${e.message}")
        }
    }
    
    /**
     * Tạo bitmap placeholder an toàn
     */
    fun createPlaceholderBitmap(width: Int, height: Int, color: Int): Bitmap? {
        return try {
            if (width <= 0 || height <= 0) {
                Log.w(TAG, "Invalid dimensions for placeholder: ${width}x${height}")
                return null
            }
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(color)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error creating placeholder bitmap: ${e.message}")
            null
        }
    }
}

package com.itsthwng.twallpaper.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import android.graphics.drawable.BitmapDrawable
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.itsthwng.twallpaper.R

class GlideLoader(private val mContext: Context?) {
    fun loadImage(imageUrl: String?, imageView: ImageView?) {
        if (mContext != null && imageView != null) {
            // Kiểm tra xem ImageView có hợp lệ không
            if (!BitmapUtils.isImageViewValid(imageView)) {
                android.util.Log.w("GlideLoader", "loadImage: ImageView is invalid")
                return
            }
            
            Glide.with(mContext).load(imageUrl).apply(
                RequestOptions().error(
                    R.color.color_premium
                ).priority(Priority.HIGH)
                    .dontTransform() // Tránh transform bitmap để giảm lỗi
                    .timeout(30000) // 30 giây timeout
            )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Logger.e("GlideError", "Load failed for $imageUrl", e)
                        return false // Trả về false để tiếp tục xử lý error fallback (hiển thị màu)
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Kiểm tra xem resource có hợp lệ không
                        if (!BitmapUtils.isDrawableValid(resource)) {
                            android.util.Log.w("GlideLoader", "Resource is not valid, skipping")
                            return false
                        }
                        
                        // Log trạng thái bitmap để debug
                        if (resource is BitmapDrawable) {
                            BitmapUtils.logBitmapStatus("GlideLoader", resource.bitmap, "resource ready")
                        }
                        
                        return false
                    }
                })
                .into(imageView)
        }
    }

    fun loadNotificationImage(drawable: Drawable?, imageView: ImageView?) {
        if (imageView != null && drawable != null) {
            // Kiểm tra xem drawable có hợp lệ không
            if (!BitmapUtils.isDrawableValid(drawable)) {
                android.util.Log.w("GlideLoader", "Notification drawable is not valid")
                return
            }
            
            // Log trạng thái bitmap để debug
            if (drawable is BitmapDrawable) {
                BitmapUtils.logBitmapStatus("GlideLoader", drawable.bitmap, "notification image")
            }
            
            imageView.setImageDrawable(drawable)
        }
    }

    //    void loadMediaImage(String imageUrl, ImageView imageView) {
    //        if (mContext != null && imageView != null) {
    //
    //            Glide.with(mContext).load(new File(imageUrl)).apply(
    //                    new RequestOptions()
    //                            .placeholder(circularProgressDrawable).error(
    //                                    R.color.transparent
    //                            ).priority(Priority.HIGH)
    //            ).into(imageView);
    //        }
    //    }
    fun loadBlurImage(imageUrl: String?, imageView: ImageView?) {
        if (mContext != null && imageView != null) {
            // Kiểm tra xem ImageView có hợp lệ không
            if (!BitmapUtils.isImageViewValid(imageView)) {
                android.util.Log.w("GlideLoader", "loadBlurImage: ImageView is invalid")
                return
            }
            
            Glide.with(mContext).load(imageUrl)
                .transform(BlurTransformation(20))
                .apply(
                    RequestOptions().error(
                        R.color.transparent
                    ).priority(Priority.HIGH)
                        .dontTransform() // Tránh transform bitmap để giảm lỗi
                        .timeout(30000) // 30 giây timeout
                )
                .into(imageView)
        }
    }


    fun loadImageWithError(imageUrl: String?, imageView: ImageView?, errorDrawable: Int) {
        if (mContext != null && imageView != null) {
            Glide.with(mContext).load(imageUrl).thumbnail(0.25f).apply(
                RequestOptions()
                    .error(errorDrawable)
                    .dontAnimate()
                    .dontTransform()
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .placeholder(R.color.color_shimmer)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Logger.e("GlideError", "Load failed for $imageUrl", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        }
    }
}
package com.itsthwng.twallpaper.utils

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.databinding.BindingAdapter
import com.itsthwng.twallpaper.R

class BindingAdapters {
    companion object {

        @JvmStatic
        @BindingAdapter("app:tint_img")
        fun setImageTint(view: ImageView, @ColorInt color: Int) {
            view.setColorFilter(color)
        }

        @JvmStatic
        @BindingAdapter("app:background_res")
        fun setImageFilterViewBackGroundResource(view: ImageFilterView, backgroundResId: Int) {
            view.setBackgroundResource(backgroundResId)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        @JvmStatic
        @BindingAdapter("app:background_res")
        fun setLinearLayoutBackGroundResource(view: LinearLayout, backgroundResId: Int) {
            view.setBackgroundResource(backgroundResId)
        }

        @JvmStatic
        @BindingAdapter("app:background_color")
        fun setImageFilterViewBackGroundColor(view: ImageFilterView, color: Int) {
            view.setBackgroundColor(color)
        }

        @JvmStatic
        @BindingAdapter("app:text_color")
        fun setImageTint(view: TextView, @ColorInt color: Int) {
            view.setTextColor(color)
        }

        @JvmStatic
        @BindingAdapter("app:background_alpha_textview")
        fun setTextBackgroundAlphaTextView(view: TextView, alpha: Int) {
            view.background.alpha = alpha
        }

        @JvmStatic
        @BindingAdapter("app:background_alpha_relative")
        fun setTextBackgroundAlphaRelative(view: RelativeLayout, alpha: Int) {
            view.background.alpha = alpha
        }


        @JvmStatic
        @BindingAdapter("app:background_tint")
        fun setBacTint(view: TextView, @ColorInt color: Int) {
            view.setTextColor(color)
        }


        @JvmStatic
        @BindingAdapter("app:add_view")
        fun addView(layout: LinearLayout, view: View) {
            layout.addView(view)
        }


        @JvmStatic
        @BindingAdapter("image_url", "error_drawable", requireAll = false)
        fun loadImage(view: ImageView, image_url: String?, errorDrawable: Int = R.drawable.ic_image) {
            if (image_url != null && !TextUtils.isEmpty(image_url)) {
                val url = UrlHelper.getFullUrl(image_url)
                GlideLoader(view.context).loadImageWithError(url, view, errorDrawable)
            } else {
                view.setImageResource(errorDrawable)
            }
        }

//        @JvmStatic
//        @BindingAdapter("app:background_alpha_pie")
//        fun setChartBackGround(chart: PieChart, alpha: Int) {
//            chart.background.alpha = alpha
//
//        }


    }
}
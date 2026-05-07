package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity

class ZipperViewPagerAdapter(
    private val context: Context,
    private var zippers: List<ZipperImageEntity> = emptyList(),
    private var currentWallpaperId: Int = -1,
    private var currentWallpaperBgId: Int = -1,
    private var type: Int = 0 // 0 for foreground, 1 for background
) : PagerAdapter() {

    private var selectedPosition = 0

    fun updateData(newZippers: List<ZipperImageEntity>) {
        this.zippers = newZippers
        notifyDataSetChanged()
    }

    fun updateOneZipper(updatedZipper: ZipperImageEntity) {
        val index = zippers.indexOfFirst { it.id == updatedZipper.id }
        if (index != -1) {
            val mutableList = zippers.toMutableList()
            mutableList[index] = updatedZipper
            zippers = mutableList
            notifyDataSetChanged()
        }
    }

    fun attachTo(viewPager: ViewPager) { this.hostViewPager = viewPager } // gọi từ ngoài sau khi set adapter
    private var hostViewPager: ViewPager? = null

    fun updateSelectedPosition(position: Int) {
        if (position == selectedPosition) return
        val old = selectedPosition
        selectedPosition = position

        hostViewPager?.let { vp ->
            (vp.findViewWithTag<View>("page_$old"))?.let {
                val oldZipper = getZipperAtPosition(old)
                val isOldSet = oldZipper?.let {
                    when (type) {
                        0 -> currentWallpaperId == it.id
                        1 -> currentWallpaperBgId == it.id
                        else -> false
                    }
                } ?: false
                bindSelectionUi(it, old, isOldSet)
            }
            (vp.findViewWithTag<View>("page_$position"))?.let {
                val currentZipper = getZipperAtPosition(position)
                val isCurrentSet = currentZipper?.let {
                    when (type) {
                        0 -> currentWallpaperId == it.id
                        1 -> currentWallpaperBgId == it.id
                        else -> false
                    }
                } ?: false
                bindSelectionUi(it, position, isCurrentSet)
            }
        }
//        this.selectedPosition = position
//        notifyDataSetChanged()
    }

    override fun getCount(): Int = zippers.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun getPageTitle(position: Int): CharSequence = ""

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_wallpaper, container, false)
        view.tag = "page_$position"

        // Check if this item is already set as wallpaper
        val zipper = zippers[position]
        val isCurrentlySet = when (type) {
            0 -> currentWallpaperId == zipper.id
            1 -> currentWallpaperBgId == zipper.id
            else -> false
        }

        bindSelectionUi(view, position, isCurrentlySet) // hàm riêng set check/viền theo selectedPosition

        val imageView = view.findViewById<ImageView>(R.id.recommended_wallpaper_image)
        val btnIconPremium = view.findViewById<ImageView>(R.id.btnIconPremium)
        val btnIconLock = view.findViewById<ImageView>(R.id.btnIconLock)
        val tvCurrentCoins = view.findViewById<TextView>(R.id.tvCurrentCoins)
        val llCoin = view.findViewById<LinearLayout>(R.id.llCoin)
        // Load image from content URL or fallback to drawable
        Log.d("NINVB", "instantiateItem: zipper content ${zipper.content}")
        if (zipper.content.isNotEmpty()) {
            // Content should always be a full URL from Firebase
            Glide.with(context)
                .load(zipper.content)
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.color.color_shimmer)
                        .error(R.color.color_shimmer)
                )
                .into(imageView)
        } else {
            // Fallback to drawable resource
            try {
                val drawableId = context.resources.getIdentifier(
                    "image${zipper.id}",
                    "drawable",
                    context.packageName
                )
                if (drawableId != 0) {
                    imageView.setImageResource(drawableId)
                } else {
                    imageView.setImageResource(R.color.color_shimmer)
                }
            } catch (e: Exception) {
                imageView.setImageResource(R.color.color_shimmer)
            }
        }

        when (zipper.accessType) {
            0 -> {
                btnIconPremium.visibility = View.VISIBLE
                btnIconLock.visibility = View.GONE
            }
            1 -> {
                btnIconPremium.visibility = View.GONE
                btnIconLock.visibility = View.VISIBLE
            }
            else -> {
                btnIconPremium.visibility = View.GONE
                btnIconLock.visibility = View.GONE
            }
        }
        if(zipper.pricePoints > 0 && zipper.accessType != 2){
            llCoin.visibility = View.VISIBLE
            tvCurrentCoins.text = zipper.pricePoints.toString()
        } else {
            llCoin.visibility = View.GONE
        }

        imageView.setOnClickListener {
            updateSelectedPosition(position)
        }

        container.addView(view)
        return view
    }

    private fun bindSelectionUi(view: View, position: Int, isCurrentlySet: Boolean = false) {
        val checkImage = view.findViewById<ImageView>(R.id.recommended_wallpaper_check_image)
        val frame = view.findViewById<FrameLayout>(R.id.imageContainer)

        if (position == selectedPosition) {
            // When selected: show white border and tick
            checkImage.visibility = View.VISIBLE
            frame.setBackgroundResource(R.drawable.bg_selected_zipper_image) // White border
            // Add a small indicator for already set wallpaper
            if (isCurrentlySet) {
                checkImage.alpha = 0.7f
                checkImage.setImageResource(R.drawable.personalisation_check)
            }
        } else {
            // When not selected: remove border and hide tick
            checkImage.visibility = View.GONE
            frame.setBackgroundResource(0) // Remove background
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    fun getSelectedZipper(): ZipperImageEntity? {
        return if (selectedPosition in 0 until zippers.size) zippers[selectedPosition] else null
    }

    fun getZipperAtPosition(position: Int): ZipperImageEntity? {
        return if (position in 0 until zippers.size) zippers[position] else null
    }
}
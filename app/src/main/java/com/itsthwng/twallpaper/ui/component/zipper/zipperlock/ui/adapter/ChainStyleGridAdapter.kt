package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import java.util.Collections

class ChainStyleGridAdapter(
    private val context: Context,
    private var chains: List<String>? = null,
    private var zipperList: MutableList<ZipperImageEntity> = Collections.emptyList(),
    private var selectedPosition: Int,
    private var isUsingZipperList: Boolean = false
) : BaseAdapter() {

    fun updateData(newZipperList: MutableList<ZipperImageEntity>) {
        this.zipperList = newZipperList
        this.isUsingZipperList = true
        this.chains = null
        notifyDataSetChanged()
    }

    fun update(i: Int) {
        this.selectedPosition = i
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        val count: Int
        if (isUsingZipperList) {
            count = this.zipperList.size
            println("getCount() - using zipperList: $count")
        } else {
            count = if (this.chains != null) this.chains!!.size else 0
            println("getCount() - using heart_zipper_chains: $count")
        }
        return count
    }

    override fun getItem(i: Int): Any? {
        return if (isUsingZipperList) {
            if (i < this.zipperList.size) this.zipperList[i] else null
        } else {
            if (this.chains != null && i < this.chains!!.size) this.chains!![i] else null
        }
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    @SuppressLint("WrongConstant")
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
        var view = view
        println("getView() called for position: $i")

        if (view == null) {
            view = LayoutInflater.from(this.context)
                .inflate(R.layout.item_chain2, null as ViewGroup?)
            println("New view created for position: $i")
        }

        val imageView = view.findViewById<View?>(R.id.chain_image) as ImageView
        val imageView2 = view.findViewById<View?>(R.id.chain_check_image) as ImageView
        val imageView3 = view.findViewById<View?>(R.id.imageView3) as ImageView
        val textView = view.findViewById<View?>(R.id.chain_title) as TextView?

        if (isUsingZipperList) {
            // Sử dụng ZipperImageEntity
            if (i < this.zipperList.size) {
                val zipper = this.zipperList[i]
                println("Loading zipper entity: " + zipper.content)

                // Sử dụng contentLeft cho chainLeft và contentRight cho chainRight
                var imageUrl: String? = null
                if (!zipper.contentLeft.isEmpty()) {
                    imageUrl = zipper.contentLeft
                    println("Using contentLeft: $imageUrl")
                } else if (!zipper.contentRight.isEmpty()) {
                    imageUrl = zipper.contentRight
                    println("Using contentRight: $imageUrl")
                } else if (!zipper.content.isEmpty()) {
                    imageUrl = zipper.content
                    println("Using content as fallback: $imageUrl")
                }

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this.context)
                        .load(imageUrl)
                        .placeholder(R.color.color_shimmer)
                        .error(R.color.color_shimmer)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.color.color_shimmer)
                }

                if (textView != null) {
                    textView.text = zipper.name
                }
            }
        } else {
            // Sử dụng String array (fallback)
            if (this.chains != null && i < this.chains!!.size) {
                val zipper = this.chains!![i]
                println("Loading string resource: $zipper")

                // Load từ drawable resource
                try {
                    val resourceId = this.context.resources.getIdentifier(
                        zipper,
                        "drawable",
                        this.context.packageName
                    )
                    if (resourceId != 0) {
                        imageView.setImageResource(resourceId)
                        println("Resource loaded: $resourceId")
                    } else {
                        imageView.setImageResource(R.drawable.chainleft)
                        println("Resource not found, using fallback")
                    }
                } catch (e: Exception) {
                    imageView.setImageResource(R.drawable.chainleft)
                    println("Error loading resource: " + e.message)
                }

                if (textView != null) {
                    textView.text = zipper
                }
            }
        }

        if (this.selectedPosition == i) {
            imageView2.visibility = View.VISIBLE
            imageView3.setImageResource(R.drawable.bg_main_zipper_selected)
        } else {
            imageView3.setImageResource(R.drawable.s1)
            imageView2.visibility = View.INVISIBLE
        }

        return view
    }
}
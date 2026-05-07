package com.itsthwng.twallpaper.ui.component.home.adapter

import android.animation.ValueAnimator
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ItemFeaturedPosBinding

class FeatureDotsAdapter : RecyclerView.Adapter<FeatureDotsAdapter.ItemHolder>() {
    var lastSelected = 0
    var currantSelected = 0
    private var mList: List<String> = ArrayList()
    var activity: Activity? = null
    fun getmList(): List<String> {
        return mList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_featured_pos, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setModal(position)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list: List<String>) {
        mList = list
        notifyDataSetChanged()
    }

    fun scrollToPos(pos: Int) {
        if (getmList().isNotEmpty()) {
            lastSelected = currantSelected
            currantSelected = pos
            notifyItemChanged(currantSelected)
            notifyItemChanged(lastSelected)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemFeaturedPosBinding? = DataBindingUtil.bind(itemView)

        fun setModal(position: Int) {
            mList[position]
            if (position == currantSelected) {
                val anim = ValueAnimator.ofInt(100)
                anim.addUpdateListener { valueAnimator ->
                    val v = valueAnimator.animatedValue as Int
                    val layoutParams = binding!!.imgView.layoutParams
                    layoutParams.width = v
                    binding!!.imgView.layoutParams = layoutParams
                    binding!!.imgView.backgroundTintList = ContextCompat.getColorStateList(
                        itemView.context,
                        R.color.color_text_primary
                    )
                }
                anim.duration = 200
                anim.start()
            } else {
                val layoutParams = binding!!.imgView.layoutParams
                layoutParams.width = 50
                binding!!.imgView.layoutParams = layoutParams
                binding!!.imgView.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.color_text_hint)

            }
        }
    }
}
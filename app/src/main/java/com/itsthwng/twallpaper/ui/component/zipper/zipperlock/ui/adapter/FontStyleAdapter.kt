package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FontStyleItemBinding

class FontStyleAdapter(
    private val context: Context,
    private var selectedPosition: Int,
    private val onClickListener: (((Int) -> Unit)) = {}
) : ListAdapter<String, FontStyleAdapter.FontStyleViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): FontStyleViewHolder {
        val binding = FontStyleItemBinding.inflate(LayoutInflater.from(context), viewGroup, false)
        return FontStyleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FontStyleViewHolder, i: Int) {
        holder.bindAt(i)
    }

    private fun getStringResourceByName(str: String?): String {
        return context.getString(this.context.resources
            .getIdentifier(str, "string", this.context.packageName)
        )
    }

    fun update(i: Int) {
        this.selectedPosition = i
        notifyDataSetChanged()
    }

    inner class FontStyleViewHolder internal constructor(val binding: FontStyleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindAt(position: Int) {
            try {
                binding.fontStyleTitle.text = getStringResourceByName(getItem(position))
                val assets = context.assets
                binding.fontStyleTitle.setTypeface(
                    Typeface.createFromAsset(assets, "fonts/" + getItem(position) + ".ttf")
                )
                if (position == selectedPosition) {
                    binding.fontBtn.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_main_zipper_selected
                    )
                    binding.fontCheckImage.visibility = View.VISIBLE
                    return
                }
                binding.fontBtn.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.s1
                )
                binding.fontCheckImage.visibility = View.GONE

                itemView.setOnClickListener {
                    onClickListener(bindingAdapterPosition)
                }
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}
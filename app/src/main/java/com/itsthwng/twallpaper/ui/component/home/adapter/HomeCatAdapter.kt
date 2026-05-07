package com.itsthwng.twallpaper.ui.component.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemHomeCatBinding

class HomeCatAdapter : RecyclerView.Adapter<HomeCatAdapter.ItemHolder>() {
    var lastSelected = 0
    var currantSelected = 0
    var mList: MutableList<SettingData.CategoriesItem> = mutableListOf()
    var onItemClick: OnItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemHomeCatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ItemHolder, position: Int
    ) {
        holder.setModal(position)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    interface OnItemClick {
        fun onClick(item: SettingData.CategoriesItem)
    }

    fun updateData(
        list: MutableList<SettingData.CategoriesItem>,
        allTitle: String
    ) {
        val catAll = SettingData.CategoriesItem(null, null, null, "new", allTitle, 0)
        mList = list
        mList.add(0, catAll)
        notifyDataSetChanged()
    }

    fun setToFirst(item: SettingData.CategoriesItem) {


    }


    inner class ItemHolder(val binding: ItemHomeCatBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setModal(position: Int) {
            val item = mList[position]

            item.title.let {
                binding.tvName.text = it
            }

            if (position == currantSelected) {

                binding.tvName.setBackgroundResource(
                    R.drawable.bg_category_selected
                )

            } else {
                binding.tvName.setBackgroundResource(
                    R.drawable.ripple_circle_category
                )
            }

            binding.tvName.setOnClickListener {

                if (position != currantSelected) {
                    lastSelected = currantSelected
                    currantSelected = position
                    notifyItemChanged(currantSelected)
                    notifyItemChanged(lastSelected)
                    onItemClick?.onClick(item)
                }
            }

        }
    }
}
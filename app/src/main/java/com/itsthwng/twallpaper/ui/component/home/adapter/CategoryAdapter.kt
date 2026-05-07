package com.itsthwng.twallpaper.ui.component.home.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemCategoryBinding
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.wallpaperByCat.WallpaperByCatActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger

class CategoryAdapter(
    private val localStorage: LocalStorage,
    private val activity: FragmentActivity,
) : ListAdapter<SettingData.CategoriesItem, CategoryAdapter.ItemViewHolder>(DIFF_CALLBACK) {

    var lastSelectedPos = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        holder.bindAt(position)
    }

    inner class ItemViewHolder(internal val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindAt(position: Int) {
            val item = getItem(position) ?: return
            binding.model = item

            if (activity is MainActivity) {
                activity.setBlur(binding.blurView, binding.rootLout)
            }

            binding.root.setOnClickListener {
                try {
                    val isFirstTimeSelectCategory = localStorage.isFirstTimeSelectCategory
                    if (isFirstTimeSelectCategory) {
                        AppConfig.logEventTracking(Constants.EventKey.SELECT_CATEGORY_1ST)
                        localStorage.isFirstTimeSelectCategory = false
                    } else {
                        AppConfig.logEventTracking(Constants.EventKey.SELECT_CATEGORY_2ND)
                    }
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_CATEGORY_DETAIL)
                    AppConfig.logEventTracking(
                        Constants.EventKey.SELECT_CATEGORY + (item.title ?: "")
                    )
                } catch (_: Exception) {
                }

                val ctx = itemView.context
                val i = Intent(ctx, WallpaperByCatActivity::class.java)
                    .putExtra(Constants.data, Gson().toJson(item))
                ctx.startActivity(i)

                // Set last position
                lastSelectedPos = position
            }
            binding.executePendingBindings()
        }
    }

    companion object {
        const val TAG = "CategoryAdapter"
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SettingData.CategoriesItem>() {
            override fun areItemsTheSame(
                oldItem: SettingData.CategoriesItem,
                newItem: SettingData.CategoriesItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SettingData.CategoriesItem,
                newItem: SettingData.CategoriesItem
            ): Boolean {
                return oldItem == newItem
            }
        }

    }
}

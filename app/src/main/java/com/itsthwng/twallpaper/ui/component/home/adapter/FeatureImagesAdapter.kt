package com.itsthwng.twallpaper.ui.component.home.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemFeaturedBinding
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.ui.component.viewWallpaper.ViewWallpapersActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger

class FeatureImagesAdapter : RecyclerView.Adapter<FeatureImagesAdapter.ItemHolder>() {
    var lastSelected = 0
    var currantSelected = 0
    var mList: List<SettingData.WallpapersItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_featured, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemHolder, position: Int
    ) {
        holder.setModal(position)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun hasData(): Boolean {
        return mList.isNotEmpty()
    }

    fun updateData(list: List<SettingData.WallpapersItem>) {
        mList = list
        notifyDataSetChanged()
    }

    fun scrollToPos(pos: Int) {
        lastSelected = currantSelected
        currantSelected = pos
        notifyItemChanged(currantSelected)
        notifyItemChanged(lastSelected)
    }

    inner class ItemHolder
        (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemFeaturedBinding = DataBindingUtil.bind(itemView)!!

        fun setModal(position: Int) {
            val item = mList[position]
            binding.model = item


            binding.root.setOnClickListener {
                try {
                    val localStorage = LocalData(binding.root.context, "sharedPreferences")
                    val isFirstTimeSelectWallpaper = localStorage.isFirstTimeSelectWallpaper
                    if (isFirstTimeSelectWallpaper) {
                        AppConfig.logEventTracking(Constants.EventKey.SELECT_WALLPAPER_1ST)
                        localStorage.isFirstTimeSelectWallpaper = false
                    } else {
                        AppConfig.logEventTracking(Constants.EventKey.SELECT_WALLPAPER_2ND)
                    }
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_VIEW_WALLPAPER)
                    AppConfig.logEventTracking(Constants.EventKey.SELECT_WALLPAPER + "${item.id}")
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                val intent = Intent(itemView.context, ViewWallpapersActivity::class.java)
                val localStorage = LocalData(binding.root.context, "sharedPreferences")
                val favIds = Global.convertStringToLis(localStorage.favourites).toSet()
                val syncedList = mList.map { wall ->
                    wall.copy(isFavorite = favIds.contains(wall.id ?: 0))
                }

                intent.putExtra(Constants.dataList, Gson().toJson(syncedList))
                intent.putExtra(Constants.position, position)
                itemView.context.startActivity(intent)
            }

        }
    }
}
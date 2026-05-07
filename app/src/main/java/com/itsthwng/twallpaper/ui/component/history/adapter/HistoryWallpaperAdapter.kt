package com.itsthwng.twallpaper.ui.component.history.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemHistoryWallpaperBinding
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.ui.component.viewWallpaper.ViewWallpapersActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger

class HistoryWallpaperAdapter : RecyclerView.Adapter<HistoryWallpaperAdapter.ItemHolder>() {

    var mList: MutableList<SettingData.WallpapersItem> = mutableListOf()
    var onFavClick: (SettingData.WallpapersItem) -> Unit = {}
    var onDeleteClick: (SettingData.WallpapersItem) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_wallpaper, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        println("HistoryAdapter: onBindViewHolder called for position=$position")
        holder.setModal(position)
    }
    
    override fun onBindViewHolder(holder: ItemHolder, position: Int, payloads: MutableList<Any>) {
        println("HistoryAdapter: onBindViewHolder with payloads called for position=$position, payloads=$payloads")
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int = mList.size

    fun updateData(list: List<SettingData.WallpapersItem>) {
        mList.clear()
        mList = list.toMutableList()
        notifyDataSetChanged()
    }
    

    fun removeItem(position: Int): SettingData.WallpapersItem? {
        return if (position >= 0 && position < mList.size) {
            val item = mList.removeAt(position)
            notifyItemRemoved(position)
            item
        } else null
    }

    fun restoreItem(item: SettingData.WallpapersItem, position: Int) {
        if (position >= 0 && position <= mList.size) {
            mList.add(position, item)
            notifyItemInserted(position)
        }
    }

    fun refreshData(favIds: List<Int>) {
        // Update only items whose favorite status has changed
        mList.forEachIndexed { index, item ->
            val newFavStatus = favIds.contains(item.id ?: 0)
            if (item.isFavorite != newFavStatus) {
                // Replace the item with updated favorite status
                mList[index] = item.copy(isFavorite = newFavStatus)
                // Notify only this specific item changed
                notifyItemChanged(index)
            }
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemHistoryWallpaperBinding = DataBindingUtil.bind(itemView)!!

        fun setModal(position: Int) {
            println("HistoryAdapter: setModal called for position=$position")
            val item = mList[position]
            
            println("HistoryAdapter: Binding item ${item.id} at position $position")
            
            binding.isFav = item.isFavorite
            binding.isSelectionMode = false
            binding.isSelected = false
            
            // Show history indicators
            binding.icHome.visibility = if (item.isSelectedHome == 3 || item.isSelectedHome == 1) View.VISIBLE else View.GONE
            binding.icLock.visibility = if (item.isSelectedLock == 3 || item.isSelectedLock == 1) View.VISIBLE else View.GONE
            
            // Show "Current" badge for wallpapers that are currently set
            val showCurrentBadge = item.isSelectedHome == 1 || item.isSelectedLock == 1
            binding.tvCurrentBadge.visibility = if (showCurrentBadge) View.VISIBLE else View.GONE
            
            // Debug current badge
            println("HistoryAdapter: Item ${item.id} - isSelectedHome=${item.isSelectedHome}, isSelectedLock=${item.isSelectedLock}")
            println("HistoryAdapter: Item ${item.id} - showCurrentBadge=$showCurrentBadge")

            // Setup click listeners
            println("HistoryAdapter: Setting up click listeners for item ${item.id}")
            
            binding.icFavYes.setOnClickListener {
                println("HistoryAdapter: Favorite clicked for item ${item.id}")
                // Let ViewModel handle the toggle
                onFavClick.invoke(item)
            }

            binding.rootLout.setOnClickListener { view ->
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_VIEW_WALLPAPER)
                    AppConfig.logEventTracking(Constants.EventKey.SELECT_WALLPAPER + "${item.id}")
                } catch (e : Exception){
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                // Simple click always opens wallpaper detail view
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
            
            // Remove long press listener - no selection mode needed
            binding.rootLout.setOnLongClickListener(null)

            binding.model = item
            
            // Force binding to update immediately
            binding.executePendingBindings()
        }
        
    }
}


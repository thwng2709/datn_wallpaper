package com.itsthwng.twallpaper.ui.component.home.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemHomeImageByCatBinding
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.ui.component.viewWallpaper.ViewWallpapersActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger

class WallpaperAdapter(
) : RecyclerView.Adapter<WallpaperAdapter.ItemHolder>() {
    var mList: MutableList<SettingData.WallpapersItem> = mutableListOf()
    private var fullDataList: MutableList<SettingData.WallpapersItem> = mutableListOf()
    var favList: ArrayList<Int> = ArrayList()
    var onFavClick: (SettingData.WallpapersItem) -> Unit = {}

    // DiffUtil để tối ưu hóa update data
    private val diffCallback = object : DiffUtil.ItemCallback<SettingData.WallpapersItem>() {
        override fun areItemsTheSame(
            oldItem: SettingData.WallpapersItem,
            newItem: SettingData.WallpapersItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SettingData.WallpapersItem,
            newItem: SettingData.WallpapersItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemHomeImageByCatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun removeWhenUpdateData(list: List<SettingData.WallpapersItem>) {
        val oldList = mList.toList()
        mList.clear()
        mList = list.toMutableList()

        // Sử dụng DiffUtil để update hiệu quả
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = mList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == mList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == mList[newItemPosition]
            }
        })

        diffResult.dispatchUpdatesTo(this)
    }

    fun updateData(list: List<SettingData.WallpapersItem>) {
        val oldList = mList.toList()
        mList.clear()
        mList = list.toMutableList()

        if (mList.isEmpty()) {
            notifyDataSetChanged()
        } else {
            // Sử dụng DiffUtil để update hiệu quả
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldList.size
                override fun getNewListSize(): Int = mList.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldList[oldItemPosition].id == mList[newItemPosition].id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldList[oldItemPosition] == mList[newItemPosition]
                }
            })

            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun loadMore(data: MutableList<SettingData.WallpapersItem>) {
        val oldSize = mList.size
        mList.addAll(data)
        notifyItemRangeInserted(oldSize, data.size)
    }

    fun clear() {
        val oldSize = mList.size
        mList.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    fun refreshData(newList: List<Int>) {
        val oldListOfFav = ArrayList<Int>()
        oldListOfFav.addAll(favList)
        favList = ArrayList()
        favList.addAll(newList)
        Log.i("TAG", "refreshData: $oldListOfFav")
        Log.i("TAG", "refreshData: fav list $favList")

        // Chỉ update những item có thay đổi favorite status
        for (i in 0..<mList.size) {
            if (oldListOfFav.contains(mList[i].id) != favList.contains(mList[i].id)) {
                Log.i("TAG", "refreshData: " + mList[i].id)
                mList[i].isFavorite = favList.contains(mList[i].id)
                notifyItemChanged(i)
            }
        }
    }

    fun updateAccessTypeData(allList: List<SettingData.WallpapersItem>) {
        val oldList = mList.toList()
        val newList = buildPatchedList(oldList, allList)

        if (mList.isEmpty()) {
            notifyDataSetChanged()
        } else {
            // Sử dụng DiffUtil để update hiệu quả
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldList.size
                override fun getNewListSize(): Int = newList.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldList[oldItemPosition].id == newList[newItemPosition].id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return oldList[oldItemPosition] == newList[newItemPosition]
                }
            })
            mList.clear()
            mList.addAll(newList)

            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun buildPatchedList(
        mList: List<SettingData.WallpapersItem>,
        allList: List<SettingData.WallpapersItem>
    ): List<SettingData.WallpapersItem> {
        val map = allList.associateBy { it.id }
        return mList.map { cur ->
            val newer = map[cur.id]
            if (newer != null) cur.patchFrom(newer) else cur
        }
    }

    private fun SettingData.WallpapersItem.patchFrom(newer: SettingData.WallpapersItem): SettingData.WallpapersItem {
        return this.copy(
            accessType = newer.accessType,
            isFavorite = newer.isFavorite
        )
    }

    // Set full data list (cho WallpaperByCat để load full data)
    fun updateFullDataList(list: List<SettingData.WallpapersItem>) {
        fullDataList.clear()
        this.fullDataList = list.toMutableList()
    }

    // Clear full data list (cho SearchActivity khi search mới)
    fun clearFullDataList() {
        fullDataList.clear()
    }

    inner class ItemHolder(val binding: ItemHomeImageByCatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setModal(position: Int) {
            val localStorage = LocalData(binding.root.context, "sharedPreferences")
            val item = mList[position]

            if (item.pricePoints <= 0) {
                binding.llCoin.visibility = View.GONE
            } else {
                binding.llCoin.visibility = View.VISIBLE
                binding.tvCurrentCoins.text = item.pricePoints.toString()
            }

            binding.isFav = favList.contains(item.id)

            binding.icFavYes.setOnClickListener {
                val id = item.id ?: return@setOnClickListener
                val isCurrentlyFav = favList.contains(id)
                val newIsFav = !isCurrentlyFav

                if (newIsFav) {
                    try {
                        val isFirstTimeFavouriteWallpaper =
                            localStorage.isFirstTimeFavouriteWallpaper
                        if (isFirstTimeFavouriteWallpaper) {
                            AppConfig.logEventTracking(Constants.EventKey.FAVOURITE_WALLPAPER_1ST)
                            localStorage.isFirstTimeFavouriteWallpaper = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.FAVOURITE_WALLPAPER_2ND)
                        }
                    } catch (e: Exception) {
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    try {
                        AppConfig.logEventTracking(Constants.EventKey.FAVOURITE_WALLPAPER + "${item.id}")
                    } catch (e: Exception) {
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    favList.add(id)
                } else {
                    favList.remove(id)
                }

                item.isFavorite = newIsFav
                // Đồng bộ với localStorage
                localStorage.favourites =
                    com.itsthwng.twallpaper.utils.Global.listOfIntegerToString(favList) ?: ""
                onFavClick.invoke(item.copy(isFavorite = newIsFav))

                val adapterPos = bindingAdapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(adapterPos)
                }
            }

            binding.rootLout.setOnClickListener {
                try {
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
                // Sử dụng fullDataList nếu có, nếu không thì dùng mList
                val dataToUse = fullDataList.ifEmpty { mList }
                intent.putExtra(
                    Constants.dataList,
                    Gson().toJson(
                        dataToUse.map { wall -> wall.copy(isFavorite = favList.contains(wall.id)) }
                    )
                )

                // Fix: Lấy position hiện tại tại thời điểm click, không phải position cũ từ ViewHolder
                val currentPosition = mList.indexOfFirst { it.id == item.id }
                val itemPosition = if (currentPosition != -1) currentPosition else position

                intent.putExtra(Constants.position, itemPosition)
                itemView.context.startActivity(intent)
            }

            binding.model = item
            binding.btnIconLock.visibility = if (item.accessType == 1) View.VISIBLE else View.GONE
            if (CommonInfo.is_show_premium && item.accessType == 0) {
                binding.btnIconPremium.visibility = View.VISIBLE
            } else if (CommonInfo.is_show_premium && item.accessType == 1) {
                binding.btnIconLock.visibility = View.VISIBLE
            } else {
                binding.btnIconPremium.visibility = View.GONE
            }
        }
    }
}
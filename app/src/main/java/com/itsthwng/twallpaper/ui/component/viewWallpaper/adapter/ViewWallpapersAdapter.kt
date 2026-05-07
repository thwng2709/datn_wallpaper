package com.itsthwng.twallpaper.ui.component.viewWallpaper.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.islamkhsh.CardSliderAdapter
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemViewWallpapersBinding
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger

class ViewWallpapersAdapter(
    private val localStorage: LocalStorage,
    private var itemList: MutableList<SettingData.WallpapersItem> = mutableListOf(),
    private var isNetworkConnected: Boolean = true
) : CardSliderAdapter<ViewWallpapersAdapter.ItemViewHolder>() {
    companion object {
        private const val PAYLOAD_ACCESS_TYPE_CHANGED = "ACCESS_TYPE_CHANGED"
        private const val PAYLOAD_FAV_CHANGED = "FAV_CHANGED"
        const val TAG = "ViewWallpaperAdsAdapter"
    }

    var favList: ArrayList<Int> = ArrayList()
    var onFavClick: (SettingData.WallpapersItem) -> Unit = {}

    override fun bindVH(
        holder: ItemViewHolder,
        position: Int
    ) {
        holder.bindAt(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val binding =
            ItemViewWallpapersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)

    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            if (payloads.contains(PAYLOAD_ACCESS_TYPE_CHANGED)) {
                // Chỉ update access type, không rebind toàn bộ item
                updateAccessTypeOnly(holder, position)
            } else if (payloads.contains(PAYLOAD_FAV_CHANGED)) {
                updateFavOnly(holder, position)
            }
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    fun hasData(): Boolean {
        return itemList.isNotEmpty()
    }

    fun getData(): List<SettingData.WallpapersItem> {
        return itemList.filter { it.id != 0 }
    }

    fun updateData(list: List<SettingData.WallpapersItem>) {
        // Kiểm tra list có hợp lệ không
        if (list.isEmpty()) {
            Log.w(TAG, "updateData: Empty list provided")
            itemList.clear()
            notifyDataSetChanged()
            return
        }

        // So sánh list cũ và mới để quyết định có cần reset adapter không
        val shouldResetAdapter = itemList.size != list.size || itemList.firstOrNull()?.id != list.firstOrNull()?.id

        itemList.clear()
        itemList = list.toMutableList()

        if (shouldResetAdapter) {
            notifyDataSetChanged()
        } else {
            Log.d(TAG, "Updating existing data without reset")
            // Chỉ update những item cần thiết
            notifyItemRangeChanged(0, itemList.size)
        }
    }

    fun updateItemAccessTypeOnly(id: Int, newAccessType: Int) {
        val index = itemList.indexOfFirst { it.id == id }
        if (index != -1) {
            itemList[index].accessType = newAccessType
            // Sử dụng payload để chỉ update phần cần thiết
            notifyItemChanged(index, PAYLOAD_ACCESS_TYPE_CHANGED)
            Log.d(TAG, "Updated access type for item $id at position $index")
        }
    }

    fun updateFavList(list: List<Int>) {
        favList.clear()
        favList.addAll(list.toMutableList())
        itemList.forEachIndexed { index, item ->
            item.isFavorite = favList.contains(item.id)
            notifyItemChanged(index, PAYLOAD_FAV_CHANGED)
        }
    }

    private fun updateAccessTypeOnly(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        // Chỉ update những view liên quan đến access type
        updateAccessTypeViews(holder.binding, item.accessType ?: 2)
        Log.d(TAG, "Updated access type views for position $position")
    }

    private fun updateFavOnly(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        // Chỉ update những view liên quan đến access type
        holder.binding.isFav = favList.contains(item.id)
        Log.d(TAG, "Updated fav views for position $position")
    }

    fun updateDownloadStatus(position: Int, timeDownloaded: Long) {
        val item = itemList[position]
        item.isDownloaded = timeDownloaded
        Log.d(TAG, "updateDownloadStatus for position $position")
    }

    private fun updateAccessTypeViews(binding: ItemViewWallpapersBinding, accessType: Int) {
        // Update visibility của các icon access type
        binding.btnIconLock.visibility = if (accessType == 1) View.VISIBLE else View.GONE
        if (CommonInfo.is_show_premium && accessType == 0) {
            binding.btnIconPremium.visibility = View.VISIBLE
        } else if (CommonInfo.is_show_premium && accessType == 1) {
            binding.btnIconLock.visibility = View.VISIBLE
        } else {
            binding.btnIconPremium.visibility = View.GONE
        }
    }

    fun updateNetwork(isNetworkConnected: Boolean) {
        this.isNetworkConnected = isNetworkConnected
    }

    inner class ItemViewHolder(internal val binding: ItemViewWallpapersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindAt(position: Int) {
            val item = itemList[position]

            binding.model = item

            if (item.pricePoints <= 0) {
                binding.llCoin.visibility = View.GONE
            } else {
                binding.llCoin.visibility = View.VISIBLE
                binding.tvCurrentCoins.text = item.pricePoints.toString()
            }
            binding.isFav = favList.contains(item.id)
            updateAccessTypeViews(binding, item.accessType ?: 2)

            binding.icFavNo.setOnClickListener {
                onFavClick.invoke(item.copy(isFavorite = !item.isFavorite))

                if (!favList.contains(item.id)) {
                    try {
                        AppConfig.logEventTracking(Constants.EventKey.FAVOURITE_WALLPAPER + "${item.id}")
                    } catch (e: Exception) {
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    favList.add(item.id!!)
                    localStorage.favourites = Global.listOfIntegerToString(favList) ?: ""
                    notifyItemChanged(position, PAYLOAD_FAV_CHANGED)
                }
            }

            binding.icFavYes.setOnClickListener {
                onFavClick.invoke(item.copy(isFavorite = !item.isFavorite))

                if (favList.contains(item.id)) {
                    favList.remove(item.id!!)
                    localStorage.favourites = Global.listOfIntegerToString(favList) ?: ""
                    notifyItemChanged(position, PAYLOAD_FAV_CHANGED)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
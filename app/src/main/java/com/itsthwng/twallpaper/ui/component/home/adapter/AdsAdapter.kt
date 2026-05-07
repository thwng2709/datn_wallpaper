package com.itsthwng.twallpaper.ui.component.home.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ItemHomeImageByCatBinding
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.viewWallpaper.ViewWallpapersActivity
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global

class AdsAdapter(
    private val localStorage: LocalStorage,
    private var itemList: MutableList<SettingData.WallpapersItem> = mutableListOf(),
    private var isNetworkConnected: Boolean = true
) : RecyclerView.Adapter<AdsAdapter.ItemViewHolder>() {
    companion object {
        private const val PAYLOAD_FAV_CHANGED = "FAV_CHANGED"
        const val TAG = "HomeAdsAdapter"
    }

    var favList: ArrayList<Int> = ArrayList()
    var onFavClick: (SettingData.WallpapersItem) -> Unit = {}

    private var isClickEnabled = true // Fix: Flag để prevent double click

    // Fix: Method để enable lại click
    fun enableClick() {
        isClickEnabled = true
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val binding = ItemHomeImageByCatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    fun hasData(): Boolean {
        return itemList.isNotEmpty()
    }

    fun getData(): List<SettingData.WallpapersItem> {
        return itemList.filter { it.id != 0 }
    }

    fun updateData(list: List<SettingData.WallpapersItem>) {
        itemList.clear()
        itemList = list.toMutableList()
        if (itemList.isEmpty()) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(0, itemList.size)
        }
    }

    fun updateFavList(list: List<Int>) {
        favList.clear()
        favList.addAll(list.toMutableList())
        itemList.forEachIndexed { index, _ ->
            notifyItemChanged(index, PAYLOAD_FAV_CHANGED)
        }
    }

    fun updateNetwork(isNetworkConnected: Boolean) {
        this.isNetworkConnected = isNetworkConnected
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        holder.bindAt(position)
    }

    inner class ItemViewHolder(internal val binding: ItemHomeImageByCatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindAt(position: Int) {
            val item = itemList[position]

            binding.isFav = favList.contains(item.id)
            if (item.pricePoints <= 0 || item.accessType == 2) {
                binding.llCoin.visibility = View.GONE
            } else {
                binding.llCoin.visibility = View.VISIBLE
                binding.tvCurrentCoins.text = item.pricePoints.toString()
            }
            binding.icFavYes.setOnClickListener {
                val id = item.id ?: return@setOnClickListener
                val isCurrentlyFav = favList.contains(id)
                val newIsFav = !isCurrentlyFav

                if (newIsFav) {
                    favList.add(id)
                } else {
                    favList.remove(id)
                }

                item.isFavorite = newIsFav
                localStorage.favourites = Global.listOfIntegerToString(favList) ?: ""
                onFavClick.invoke(item.copy(isFavorite = newIsFav))

                val adapterPos = bindingAdapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(adapterPos, PAYLOAD_FAV_CHANGED)
                }
            }

            binding.rootLout.setOnClickListener {
                if (!isClickEnabled) return@setOnClickListener
                isClickEnabled = false

                val ctx = itemView.context
                val intent = Intent(ctx, ViewWallpapersActivity::class.java).apply {
                    putExtra(
                        Constants.dataList,
                        Gson().toJson(
                            itemList.filter { it.id != 0 }
                                .map { wall -> wall.copy(isFavorite = favList.contains(wall.id)) }
                        )
                    )
                    putExtra(Constants.position, position)
                }
                ctx.startActivity(intent)
            }

            binding.model = item
            binding.btnIconLock.visibility = View.GONE
            binding.btnIconPremium.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
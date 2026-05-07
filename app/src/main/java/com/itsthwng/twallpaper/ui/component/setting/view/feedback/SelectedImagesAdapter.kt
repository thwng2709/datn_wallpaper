package com.itsthwng.twallpaper.ui.component.setting.view.feedback

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.itsthwng.twallpaper.R


class SelectedImagesAdapter(
    private val images: MutableList<Uri>,
    private val onRemove: (Uri) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_feedback, parent, false)

        // Tính width = 1/3 width của RecyclerView
        val totalWidth = parent.measuredWidth
        val itemWidth = totalWidth / 3
        val params = RecyclerView.LayoutParams(itemWidth, RecyclerView.LayoutParams.MATCH_PARENT)
        view.layoutParams = params

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = images[position]
        holder.imageView.setImageURI(uri)

        holder.btnRemove.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < images.size) {
                val removedUri = images.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
                onRemove(removedUri)
            }
        }
    }

    override fun getItemCount(): Int = images.size

    fun addImages(newUris: List<Uri>, maxCount: Int = 3) {
        val currentSize = images.size
        val remaining = maxCount - currentSize

        if (remaining <= 0) return

        val toAdd = newUris
            .filterNot { images.contains(it) } // Tránh ảnh trùng
            .take(remaining)

        if (toAdd.isNotEmpty()) {
            val insertIndex = images.size
            images.addAll(toAdd)
            notifyItemRangeInserted(insertIndex, toAdd.size)
        }
    }
}
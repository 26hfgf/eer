package com.aibeautypro.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aibeautypro.R
import com.aibeautypro.database.BeautyRecord
import com.aibeautypro.databinding.ItemHistoryBinding
import com.aibeautypro.utils.BitmapUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class HistoryAdapter(
    private val onClick: (BeautyRecord) -> Unit
) : ListAdapter<BeautyRecord, HistoryAdapter.Holder>(Diff) {

    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: Holder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    fun close() {
        executor.shutdownNow()
    }

    inner class Holder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BeautyRecord) {
            binding.tvHistoryScore.text = item.score.toString()
            binding.tvHistoryShape.text = item.faceShape
            binding.tvHistoryDate.text = dateFormat.format(Date(item.createdAt))
            binding.tvHistorySummary.text = item.summary
            binding.imageThumb.setImageResource(R.drawable.bg_image_placeholder)

            val imageSource = item.hairImagePath ?: item.imagePath
            binding.imageThumb.tag = imageSource
            executor.execute {
                val bitmap = BitmapUtils.loadThumbnail(binding.root.context, imageSource)
                mainHandler.post {
                    if (binding.imageThumb.tag == imageSource && bitmap != null) {
                        binding.imageThumb.setImageBitmap(bitmap)
                    } else if (bitmap != null && !bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
            }
            binding.root.setOnClickListener { onClick(item) }
        }

        fun clear() {
            binding.imageThumb.tag = null
            binding.imageThumb.setImageDrawable(null)
        }
    }

    private object Diff : DiffUtil.ItemCallback<BeautyRecord>() {
        override fun areItemsTheSame(oldItem: BeautyRecord, newItem: BeautyRecord): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BeautyRecord, newItem: BeautyRecord): Boolean =
            oldItem == newItem
    }
}

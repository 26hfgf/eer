package com.aibeautypro.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aibeautypro.databinding.ItemHairTemplateBinding
import com.aibeautypro.model.HairTemplate

class HairTemplateAdapter(
    private val items: List<HairTemplate>,
    private val recommendedIds: Set<String>,
    private val onClick: (HairTemplate) -> Unit
) : RecyclerView.Adapter<HairTemplateAdapter.Holder>() {

    private var selectedId: String? = null

    fun select(id: String) {
        val previous = selectedId
        selectedId = id
        previous?.let { old ->
            val index = items.indexOfFirst { it.id == old }
            if (index >= 0) notifyItemChanged(index)
        }
        val newIndex = items.indexOfFirst { it.id == id }
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemHairTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(
        private val binding: ItemHairTemplateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HairTemplate) {
            binding.tvHairName.text = item.name
            binding.tvHairDescription.text = item.description
            binding.viewHairColor.backgroundTintList = ColorStateList.valueOf(item.baseColor)
            binding.tvRecommended.visibility = if (item.id in recommendedIds) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            val isSelected = item.id == selectedId
            binding.cardHair.isChecked = isSelected
            binding.cardHair.strokeWidth = if (isSelected) {
                (3 * binding.root.resources.displayMetrics.density).toInt()
            } else {
                0
            }
            binding.root.setOnClickListener {
                select(item.id)
                onClick(item)
            }
        }
    }
}

package com.meituy.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FilterAdapter(
    private val filters: List<FilterType>,
    private val onFilterSelected: (FilterType) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.bind(filter, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
            onFilterSelected(filter)
        }
    }

    override fun getItemCount(): Int = filters.size

    fun setSelectedFilter(filterType: FilterType) {
        val position = filters.indexOf(filterType)
        if (position != -1 && position != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
        }
    }

    inner class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val filterName: TextView = itemView.findViewById(R.id.filterName)
        private val filterThumbnail: ImageView = itemView.findViewById(R.id.filterThumbnail)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(filter: FilterType, isSelected: Boolean) {
            filterName.text = filter.displayName
            
            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.accent))
                filterName.setTextColor(itemView.context.getColor(R.color.bg_dark))
            } else {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.bg_card))
                filterName.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }
            
            val thumbnailResId = when (filter) {
                FilterType.ORIGINAL -> R.drawable.filter_original
                FilterType.RICON_FLASH -> R.drawable.filter_original
                FilterType.FLASH_FILM -> R.drawable.filter_original
                FilterType.G7X -> R.drawable.filter_original
                FilterType.FUJI_FLASH -> R.drawable.filter_original
                FilterType.GOLDEN_HOUR -> R.drawable.filter_original
                FilterType.MATAHARI_TERBENAM -> R.drawable.filter_original
                FilterType.LAMPU_KILAT_IPHONE -> R.drawable.filter_original
            }
            
            filterThumbnail.setImageResource(thumbnailResId)
        }
    }

    companion object {
        fun createDefaultList(): List<FilterType> = listOf(
            FilterType.ORIGINAL,
            FilterType.RICON_FLASH,
            FilterType.FLASH_FILM,
            FilterType.G7X,
            FilterType.FUJI_FLASH,
            FilterType.GOLDEN_HOUR,
            FilterType.MATAHARI_TERBENAM,
            FilterType.LAMPU_KILAT_IPHONE
        )
    }
}
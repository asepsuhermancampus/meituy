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
                filterName.setTextColor(itemView.context.getColor(R.color.white))
            } else {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.white))
                filterName.setTextColor(itemView.context.getColor(R.color.text_primary))
            }
            
            val thumbnailResId = when (filter) {
                FilterType.ORIGINAL -> R.drawable.filter_original
                FilterType.ENHANCE -> R.drawable.filter_enhance
                FilterType.BRIGHTNESS -> R.drawable.filter_brightness
                FilterType.CONTRAST -> R.drawable.filter_contrast
                FilterType.SATURATION -> R.drawable.filter_saturation
                FilterType.BLUR_REDUCTION -> R.drawable.filter_sharpen
                FilterType.COLOR_CORRECTION -> R.drawable.filter_color_correction
                FilterType.MEITU_STYLE -> R.drawable.filter_meitu_style
                else -> R.drawable.filter_original
            }
            
            filterThumbnail.setImageResource(thumbnailResId)
        }
    }

    companion object {
        fun createDefaultList(): List<FilterType> = listOf(
            FilterType.ORIGINAL,
            FilterType.ENHANCE,
            FilterType.BRIGHTNESS,
            FilterType.CONTRAST,
            FilterType.SATURATION,
            FilterType.BLUR_REDUCTION,
            FilterType.COLOR_CORRECTION,
            FilterType.MEITU_STYLE
        )
    }
}
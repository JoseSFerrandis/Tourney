package com.example.tourney.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.databinding.ItemThumbnailBinding

class TournamentThumbnailAdapter(
    private val thumbnails: List<Int>,
    private val onThumbnailSelected: (Int) -> Unit
) : RecyclerView.Adapter<TournamentThumbnailAdapter.ThumbnailViewHolder>() {

    class ThumbnailViewHolder(private val binding: ItemThumbnailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageResId: Int, index: Int, onThumbnailSelected: (Int) -> Unit) {
            binding.ivThumbnail.setImageResource(imageResId)
            binding.cvThumbnail.setOnClickListener {
                onThumbnailSelected(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val binding = ItemThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThumbnailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(thumbnails[position], position, onThumbnailSelected)
    }

    override fun getItemCount(): Int = thumbnails.size
}
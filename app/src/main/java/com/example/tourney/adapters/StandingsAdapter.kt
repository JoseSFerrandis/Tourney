package com.example.tourney.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.databinding.ItemStandingRowBinding

data class StandingItem(
    val nickname: String,
    val points: Float
)

class StandingsAdapter(private val items: List<StandingItem>) :
    RecyclerView.Adapter<StandingsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStandingRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStandingRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvPosition.text = (position + 1).toString()
        holder.binding.tvPlayerName.text = item.nickname
        holder.binding.tvPoints.text = item.points.toString()
    }

    override fun getItemCount(): Int = items.size
}

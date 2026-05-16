package com.example.tourney.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.databinding.ItemThemeOptionBinding

data class ThemeOption(
    val name: String,
    val key: String,
    val primaryColor: Int
)

class ThemeAdapter(
    private val options: List<ThemeOption>,
    private val onThemeSelected: (ThemeOption) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    class ThemeViewHolder(private val binding: ItemThemeOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(option: ThemeOption, onThemeSelected: (ThemeOption) -> Unit) {
            binding.btnThemeOption.text = option.name
            binding.btnThemeOption.backgroundTintList = ColorStateList.valueOf(option.primaryColor)
            binding.btnThemeOption.setOnClickListener {
                onThemeSelected(option)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val binding = ItemThemeOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThemeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(options[position], onThemeSelected)
    }

    override fun getItemCount(): Int = options.size
}
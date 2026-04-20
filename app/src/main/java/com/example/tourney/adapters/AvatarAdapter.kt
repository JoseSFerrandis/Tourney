package com.example.tourney.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.R
import com.example.tourney.databinding.ItemAvatarBinding

class AvatarAdapter(
    private val avatars: List<Int>,
    private val onAvatarSelected: (Int) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    class AvatarViewHolder(private val binding: ItemAvatarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageResId: Int, index: Int, onAvatarSelected: (Int) -> Unit) {
            binding.ivAvatar.setImageResource(imageResId)
            binding.ivAvatar.setOnClickListener {
                onAvatarSelected(index + 1) // Enviamos el número (1 a 4)
                val position = index + 1
                // Añadimos el log aquí
                Log.d("AvatarAdapter", "Se pulsó el avatar en la posición: $position")

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val binding = ItemAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(avatars[position], position, onAvatarSelected)
    }

    override fun getItemCount(): Int = avatars.size
}
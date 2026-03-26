package com.example.tourney.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.databinding.ItemUserBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User

class ParticipantAdapter(/*users: List<User>, */ private val tournament : Tournament, private val refresh : () -> Unit) :
    RecyclerView.Adapter<ParticipantAdapter.UserViewHolder>() {
        //var participantList = users as MutableList<User>

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = tournament.participantList[position]
        holder.binding.tvUserNickname.text = user.nickname
        holder.binding.participantNumber.text = (position + 1).toString() + "."

        holder.binding.btnRemove.setOnClickListener {
            tournament.participantList.removeAt(position)
            refresh.invoke()
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, tournament.participantList.size)
        }
        // TODO: hacer que funcione las fotos de los usuarios
        //holder.binding.ivUserPhoto.setImageResource(user.photo)
    }

    override fun getItemCount(): Int = tournament.participantList.size
}
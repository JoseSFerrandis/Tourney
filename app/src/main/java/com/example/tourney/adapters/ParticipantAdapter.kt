package com.example.tourney.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.databinding.ItemUserBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.User

class ParticipantAdapter(private val tournament : Tournament, private val refresh : () -> Unit) :
    RecyclerView.Adapter<ParticipantAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = tournament.participantList[position]
        holder.binding.tvUserNickname.text = user.nickname
        holder.binding.participantNumber.text = (position + 1).toString() + "."

        if(tournament.creatorId == User.actualUser?.id)
        holder.binding.btnRemove.visibility = ViewGroup.VISIBLE
        else
            holder.binding.btnRemove.visibility = ViewGroup.GONE

        holder.binding.btnRemove.isEnabled = tournament.tournamentStatus == TournamentStatus.EDITABLE

        holder.binding.btnRemove.setOnClickListener {
            tournament.removeParticipantAtPosition(position)
            refresh()
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, tournament.participantList.size)
        }
        // TODO: hacer que funcione las fotos de los usuarios
        //holder.binding.ivUserPhoto.setImageResource(user.photo)
    }

    override fun getItemCount(): Int = tournament.participantList.size
}
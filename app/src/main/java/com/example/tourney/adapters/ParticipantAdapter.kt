package com.example.tourney.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.databinding.ItemUserBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.User
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.UsersDao

class ParticipantAdapter(private val tournament : Tournament, private val refresh : () -> Unit) :
    RecyclerView.Adapter<ParticipantAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val participant = tournament.participantList[position]
        holder.binding.tvUserNickname.text = participant.nickname
        holder.binding.participantNumber.text = (position + 1).toString() + "."

        if(tournament.creatorId == User.actualUser?.id)
            holder.binding.btnRemove.visibility = ViewGroup.VISIBLE
        else
            holder.binding.btnRemove.visibility = ViewGroup.GONE

        holder.binding.btnRemove.isEnabled = tournament.tournamentStatus == TournamentStatus.EDITABLE

        holder.binding.btnRemove.setOnClickListener {
            val userId = participant.userId
            
            // 1. Eliminar de la lista del torneo (Memoria)
            tournament.removeParticipantAtPosition(position)
            
            // 2. Si es un usuario registrado, eliminar la relación de inscripción (Base de Datos)
            if (userId != null) {
                UserRepository.getInstance(UsersDao(holder.itemView.context), APIService.getInstance()).removeJoinedTournamentRelation(
                    userId, 
                    tournament.id,
                    holder.itemView.context
                )
            }
            
            // 3. Refrescar UI y persistir cambios de la lista de participantes
            refresh()
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, tournament.participantList.size)
        }
        // TODO: hacer que funcione las fotos de los usuarios
        //holder.binding.ivUserPhoto.setImageResource(participant.photo)
    }

    override fun getItemCount(): Int = tournament.participantList.size
}

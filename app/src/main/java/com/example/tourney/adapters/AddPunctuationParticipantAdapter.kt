package com.example.tourney.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.R
import com.example.tourney.databinding.ItemUserAddPuntuationBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.User
import com.ventura.bracketslib.model.CompetitorData

class AddPunctuationParticipantAdapter(private val tournament : Tournament, private val context: Context) :
    RecyclerView.Adapter<AddPunctuationParticipantAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserAddPuntuationBinding) : RecyclerView.ViewHolder(binding.root){
        var textWatcher: TextWatcher? = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserAddPuntuationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val participant = tournament.participantList[position]
        val notDeadList = tournament.getNotDead()

        val competitor = notDeadList.find { it.name == participant.nickname }
        val isAlive = competitor != null

        holder.binding.participantPoints.tag = null

        // 1. ELIMINAMOS el listener anterior si existe para evitar que actualice al competidor equivocado
        holder.textWatcher?.let {
            holder.binding.participantPoints.removeTextChangedListener(it)
        }
        holder.binding.participantPoints.setText(competitor?.score ?: "")

        setUIBasedOnStatus(holder, isAlive, position, participant)

        val newWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Actualizamos el score en el objeto competitor directamente
                competitor?.score = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        holder.textWatcher = newWatcher
        holder.binding.participantPoints.addTextChangedListener(newWatcher)

        //tournament.getNotDead()[position].score = holder.binding.participantPoints.text.toString()
    }

    private fun setUIBasedOnStatus(holder: UserViewHolder, isAlive: Boolean, position: Int, participant: User){
        when(tournament.tournamentStatus){
            TournamentStatus.EDITABLE -> {
                holder.binding.participantPoints.visibility = View.GONE
                holder.binding.participantNumber.visibility = View.GONE
            }
            TournamentStatus.IN_PROGRESS -> {
                holder.binding.participantPoints.visibility = View.VISIBLE
                holder.binding.participantPoints.isFocusable = true
                holder.binding.participantNumber.visibility = View.GONE
            }
            TournamentStatus.FINISHED -> {
                holder.binding.participantPoints.visibility = View.VISIBLE
                holder.binding.participantPoints.isEnabled = false
                holder.binding.participantNumber.visibility = View.VISIBLE
            }
        }

        holder.binding.tvUserNickname.text = participant.nickname
        holder.binding.participantNumber.text = (position + 1).toString() + "."

        holder.binding.participantPoints.isEnabled = isAlive
        // Cambia el color de la tarjeta si el participante no está vivo
        if(!isAlive){
            holder.binding.participantCard.setCardBackgroundColor(
                holder.binding.participantCard.context.resources.getColor(R.color.text_secondary)
            )
        } else {
            holder.binding.participantCard.setCardBackgroundColor(
                holder.binding.participantCard.context.resources.getColor(R.color.white)
            )
        }
    }

    override fun getItemCount(): Int = tournament.participantList.size
}
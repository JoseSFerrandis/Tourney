package com.example.tourney.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.R
import com.example.tourney.databinding.ItemUserAddPuntuationBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User

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
        val lastMatches = tournament.getLastMatchList()
        val competitor = lastMatches.flatMap { listOf(it.competitorOne, it.competitorTwo) }
            .find { it.name == participant.nickname }

        val isAlive = competitor != null

        // 1. Eliminar listener anterior
        holder.textWatcher?.let {
            holder.binding.participantPoints.removeTextChangedListener(it)
            holder.textWatcher = null
        }

        // 2. Mostrar texto según tipo y estado
        if (tournament.tournamentStatus == TournamentStatus.FINISHED) {
            if (tournament.type == TournamentType.LIGUILLA || tournament.type == TournamentType.SUIZO) {
                holder.binding.participantPoints.setText(calculateTotalScore(participant.nickname).toString())
            }
        } else {
            holder.binding.participantPoints.setText(competitor?.score ?: "")
        }
        setUIBasedOnStatus(holder, isAlive, position, participant)

        // 3. Añadir el divisor basado en los emparejamientos reales
        if(tournament.tournamentStatus == TournamentStatus.FINISHED){
            holder.binding.divider.visibility = View.GONE
        }
        else
        if (tournament.tournamentStatus == TournamentStatus.IN_PROGRESS) {
            val match = lastMatches.find { it.competitorOne.name == participant.nickname || it.competitorTwo.name == participant.nickname }
            val isSecondInMatch = match?.competitorTwo?.name == participant.nickname
            val opponentIsDescanso = (match?.competitorOne?.name == "DESCANSO" || match?.competitorTwo?.name == "DESCANSO")

            if ((isSecondInMatch || opponentIsDescanso) && position != tournament.participantList.size - 1) {
                holder.binding.divider.visibility = View.VISIBLE
            } else {
                holder.binding.divider.visibility = View.GONE
            }
        } else if ((position + 1) % 2 == 0 && position != tournament.participantList.size - 1) {
             // Lógica por defecto para otros estados
            holder.binding.divider.visibility = View.VISIBLE
        } else {
            holder.binding.divider.visibility = View.GONE
        }

        // 4. SOLO añadir el Watcher si el torneo está en progreso
        if (tournament.tournamentStatus == TournamentStatus.IN_PROGRESS && isAlive) {
            val newWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Solo actualizamos si el usuario está escribiendo realmente
                    if (holder.binding.participantPoints.hasFocus()) {
                        competitor?.score = s.toString()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            }
            holder.textWatcher = newWatcher
            holder.binding.participantPoints.addTextChangedListener(newWatcher)
        }
    }

    private fun getLastMatchScore(nickname: String): String {
        tournament.columnMatches.reversed().forEach { column ->
            val match = column.matches.find { it.competitorOne.name == nickname || it.competitorTwo.name == nickname }
            if (match != null) {
                return if (match.competitorOne.name == nickname) match.competitorOne.score else match.competitorTwo.score
            }
        }
        return "0"
    }

    private fun calculateTotalScore(nickname: String): Float {
        var total = 0f
        tournament.columnMatches.forEach { column ->
            column.matches.forEach { match ->
                if (match.competitorOne.name == nickname) {
                    total += match.competitorOne.score.toFloatOrNull() ?: 0f
                } else if (match.competitorTwo.name == nickname) {
                    total += match.competitorTwo.score.toFloatOrNull() ?: 0f
                }
            }
        }
        return total
    }

    private fun setUIBasedOnStatus(holder: UserViewHolder, isAlive: Boolean, position: Int, participant: User){
        holder.binding.tvUserNickname.text = participant.nickname
        holder.binding.participantNumber.text = (position + 1).toString() + "."

        holder.binding.participantPoints.isEnabled = isAlive


        if(isAlive || tournament.tournamentStatus == TournamentStatus.FINISHED){
            holder.binding.participantCard.setCardBackgroundColor(
                holder.binding.participantCard.context.resources.getColor(R.color.white)
            )
        }
        else{
            holder.binding.participantCard.setCardBackgroundColor(
                holder.binding.participantCard.context.resources.getColor(R.color.text_secondary)
            )
        }

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
                if(tournament.type == TournamentType.ELIMINATION){
                    holder.binding.participantPoints.visibility = View.GONE
                    holder.binding.participantNumber.visibility = View.VISIBLE
                }
                else{
                    holder.binding.participantPoints.visibility = View.VISIBLE
                    holder.binding.participantPoints.isEnabled = false
                    holder.binding.participantNumber.visibility = View.VISIBLE
                }
            }
        }
        
        if(!isAlive) holder.binding.participantPoints.visibility = View.GONE
        holder.binding.participantPoints.isEnabled = tournament.creator == User.actualUser?.nickname
    }

    override fun getItemCount(): Int = tournament.participantList.size
}

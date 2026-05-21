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
import com.example.tourney.entities.Participant
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository

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

        // 1. Limpiar listener anterior para evitar colisiones al reciclar
        holder.textWatcher?.let {
            holder.binding.participantPoints.removeTextChangedListener(it)
            holder.textWatcher = null
        }

        // 2. Definir qué texto mostrar
        if (tournament.tournamentStatus == TournamentStatus.FINISHED) {
            // Al finalizar, calculamos el total real sumando todas las jornadas
            val total = calculateTotalScore(participant.nickname)
            holder.binding.participantPoints.setText(total.toString())
            participant.puntuation = total
        } else {
            // Durante el torneo, mostramos el score del partido de la jornada actual
            val currentScore = competitor?.score ?: "0"
            holder.binding.participantPoints.setText(if (currentScore == "null") "0" else currentScore)
        }

        setUIBasedOnStatus(holder, isAlive, position, participant)

        // 3. Divisores visuales para agrupar parejas
        if (tournament.tournamentStatus == TournamentStatus.FINISHED) {
            holder.binding.divider.visibility = View.GONE
        } else if (tournament.tournamentStatus == TournamentStatus.IN_PROGRESS) {
            val match = lastMatches.find { it.competitorOne.name == participant.nickname || it.competitorTwo.name == participant.nickname }
            val isSecondInMatch = match?.competitorTwo?.name == participant.nickname
            val opponentIsDescanso = (match?.competitorOne?.name == "DESCANSO" || match?.competitorTwo?.name == "DESCANSO")

            if ((isSecondInMatch || opponentIsDescanso) && position != tournament.participantList.size - 1) {
                holder.binding.divider.visibility = View.VISIBLE
            } else {
                holder.binding.divider.visibility = View.GONE
            }
        } else {
            holder.binding.divider.visibility = if ((position + 1) % 2 == 0) View.VISIBLE else View.GONE
        }

        // 4. Sincronización en tiempo real (solo si el torneo está en curso y el jugador está activo)
        if (tournament.tournamentStatus == TournamentStatus.IN_PROGRESS && isAlive) {
            val newWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // El check de hasFocus() evita que el setText() inicial dispare cambios accidentales
                    if (holder.binding.participantPoints.hasFocus()) {
                        competitor?.score = s.toString()
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    if (holder.binding.participantPoints.hasFocus()) {
                        // Sincronizamos con el modelo plano (matches) y guardamos en DB
                        tournament.updateMatchesFromView()
                        TournamentRepository.getInstance(context).updateTournament(tournament, context, {}, {})
                    }
                }
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

    private fun setUIBasedOnStatus(holder: UserViewHolder, isAlive: Boolean, position: Int, participant: Participant){
        holder.binding.tvUserNickname.text = participant.nickname
        holder.binding.participantNumber.text = (position + 1).toString() + "."

        if(isAlive || tournament.tournamentStatus == TournamentStatus.FINISHED){
            holder.binding.participantCard.setCardBackgroundColor(
                holder.binding.participantCard.context.resources.getColor(R.color.white)
            )
        } else {
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
                } else {
                    holder.binding.participantPoints.visibility = View.VISIBLE
                    holder.binding.participantPoints.isEnabled = false
                }
                holder.binding.participantNumber.visibility = View.VISIBLE
            }
        }

        if(!isAlive && tournament.tournamentStatus != TournamentStatus.FINISHED) {
            holder.binding.participantPoints.visibility = View.GONE
        }

        // Solo el creador puede editar puntos y solo durante el torneo
        holder.binding.participantPoints.isEnabled = (
                tournament.creatorId == User.actualUser?.id 
                && tournament.tournamentStatus == TournamentStatus.IN_PROGRESS 
                && isAlive
        )
    }

    override fun getItemCount(): Int = tournament.participantList.size
}

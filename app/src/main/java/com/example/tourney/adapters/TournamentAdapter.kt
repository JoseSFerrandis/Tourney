package com.example.tourney.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.entities.Tournament
import com.example.tourney.R
import android.content.Context
import com.example.tourney.entities.TournamentStatus

class TournamentAdapter(
    private var allTournaments: MutableList<Tournament>,

    private val onTournamentClick: (Tournament) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>() {

    private var tournaments: MutableList<Tournament> = allTournaments.toMutableList()

    inner class TournamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTournamentName: TextView = itemView.findViewById(R.id.tv_tournament_name)
        val tvGameName: TextView = itemView.findViewById(R.id.tv_game_name)
        val tvCreator: TextView = itemView.findViewById(R.id.tv_creator)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
        val tvParticipants: TextView = itemView.findViewById(R.id.tv_participants)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        val tvPrize: TextView = itemView.findViewById(R.id.tv_prize)


        fun bind(tournament: Tournament) {
            val context = itemView.context
            tvTournamentName.text = establishedValue(context, tournament.name)
            tvGameName.text = establishedValue(context, tournament.game)
            tvCreator.text = establishedValue(context, tournament.creator)
            tvParticipants.text = establishedValue(context, "${tournament.numParticipants}/${tournament.maxParticipants}")
            tvDate.text = establishedValue(context, tournament.date)
            tvLocation.text = establishedValue(context, tournament.location)
            tvPrize.text = establishedValue(context, tournament.prize)

            // Inscripciones abiertas
            // En progreso
            // Finalizado

            val badgeBackground = when (tournament.tournamentStatus) {
                TournamentStatus.EDITABLE -> R.drawable.bg_badge_open
                TournamentStatus.IN_PROGRESS -> R.drawable.bg_badge_progress
                TournamentStatus.FINISHED -> R.drawable.bg_badge_finished
            }
            tvStatusBadge.setBackgroundResource(badgeBackground)

            tvStatusBadge.text = when (tournament.tournamentStatus) {
                TournamentStatus.EDITABLE -> "Abierto"
                TournamentStatus.IN_PROGRESS -> "En progreso"
                TournamentStatus.FINISHED -> "Finalizado"
            }

            itemView.setOnClickListener {
                onTournamentClick(tournament)
            }
        }
    }


    /**
    @joschajov 22/03/2025
    Esta función se ha creado con el objetivo de poder actualizar los datos del adaptador
    ya que al llegar al onResume podemos perder los datos de la lista de torneos (i
     */
    // Método para actualizar los datos
    // Actualiza la lista maestra y resetea el filtro
    fun updateTournaments(newTournaments: List<Tournament>) {
        this.allTournaments.clear()
        this.allTournaments.addAll(newTournaments)
        this.tournaments = allTournaments.toMutableList() // Resetear a la lista completa
        notifyDataSetChanged()
    }

    fun filterTournaments(query: String) {
        val filteredList = if (query.isEmpty()) {
            allTournaments // Si no hay búsqueda, mostramos todo
        } else {
            allTournaments.filter { tournament ->
                tournament.name.contains(query, ignoreCase = true) ||
                        tournament.game.contains(query, ignoreCase = true)
            }
        }

        this.tournaments = filteredList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament_card, parent, false)
        return TournamentViewHolder(view)
    }

    override fun onBindViewHolder(holder: TournamentViewHolder, position: Int) {
        holder.bind(tournaments[position])
    }

    override fun getItemCount(): Int = tournaments.size

    fun establishedValue(context: Context, value: String): String{
        return if (value.isNullOrBlank() || value == "null") {
            context.getString(R.string.no_established)
        } else {
            value
        }
    }
}

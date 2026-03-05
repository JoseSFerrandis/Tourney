package com.example.tournamentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tournamentapp.models.Tournament
import com.example.tourney.R

class TournamentAdapter(
    private val tournaments: List<Tournament>,
    private val onTournamentClick: (Tournament) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>() {

    inner class TournamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTournamentName: TextView = itemView.findViewById(R.id.tv_tournament_name)
        val tvGameName: TextView = itemView.findViewById(R.id.tv_game_name)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
        val tvParticipants: TextView = itemView.findViewById(R.id.tv_participants)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        val tvPrize: TextView = itemView.findViewById(R.id.tv_prize)

        fun bind(tournament: Tournament) {
            tvTournamentName.text = tournament.name
            tvGameName.text = tournament.game
            tvStatusBadge.text = tournament.status
            tvParticipants.text = "${tournament.participants}/${tournament.maxParticipants}"
            tvDate.text = tournament.date
            tvLocation.text = tournament.location
            tvPrize.text = tournament.prize

            // Set badge background based on status
            val badgeBackground = if (tournament.status == "En Progreso") {
                R.drawable.bg_badge_progress
            } else {
                R.drawable.bg_badge_open
            }
            tvStatusBadge.setBackgroundResource(badgeBackground)


            itemView.setOnClickListener {
                onTournamentClick(tournament)
            }
        }
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
}

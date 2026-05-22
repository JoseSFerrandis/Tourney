package com.example.tourney.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tourney.adapters.StandingItem
import com.example.tourney.adapters.StandingsAdapter
import com.example.tourney.databinding.FragmentTournamentStandingsBinding
import com.example.tourney.entities.Tournament

class TournamentStandingsFragment : Fragment() {

    private var _binding: FragmentTournamentStandingsBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTournamentStandingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tournament = arguments?.getParcelable("tournament_data")

        tournament?.let { t ->
            val standings = calculateStandings(t)
            binding.rvStandings.adapter = StandingsAdapter(standings)
        }
    }

    private fun calculateStandings(t: Tournament): List<StandingItem> {
        val pointsMap = mutableMapOf<String, Float>()
        
        // Inicializar a todos los participantes con 0 puntos
        t.participantList.forEach { pointsMap[it.nickname] = 0f }

        // Sumar puntos de todos los partidos registrados
        t.columnMatches.forEach { column ->
            column.matches.forEach { match ->
                val p1 = match.competitorOne.name
                val p2 = match.competitorTwo.name
                val s1 = match.competitorOne.score.toFloatOrNull() ?: 0f
                val s2 = match.competitorTwo.score.toFloatOrNull() ?: 0f

                // Lógica de puntos: Liguilla y Suizo suelen usar 3-1-0 o similar
                // Aquí usamos la puntuación directa del "score" como puntos si es lo que el usuario espera,
                // o sumamos victorias. Según ClasificationListFragment, sumaba los scores directos.
                if (p1 != "DESCANSO") {
                    pointsMap[p1] = (pointsMap[p1] ?: 0f) + s1
                }
                if (p2 != "DESCANSO") {
                    pointsMap[p2] = (pointsMap[p2] ?: 0f) + s2
                }
            }
        }

        return pointsMap.map { StandingItem(it.key, it.value) }
            .sortedByDescending { it.points }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
